package launcher.wb.codegeneration.convert

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.buildCodeBlock

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/19
 *
 * 代码构建器：根据匹配结果生成源类的扩展函数。
 */
class ConvertCodeBuilder {

    fun buildFileSpec(target: ConvertTarget, matchResult: ConvertPropertyMatch): FileSpec {
        val sourceSimpleName = target.sourceDecl.simpleName.asString()
        val targetSimpleName = target.targetDecl.simpleName.asString()

        val sourceClass = ClassName.bestGuess(target.sourceDecl.qualifiedName!!.asString())
        val targetClass = ClassName.bestGuess(target.targetDecl.qualifiedName!!.asString())
        val converterClass = ClassName.bestGuess(target.converterDecl.qualifiedName!!.asString())

        val funcName = target.functionName.ifEmpty { "convertTo$targetSimpleName" }
        val fileName = "${sourceSimpleName}ConvertTo${targetSimpleName}"

        val hasParent = matchResult.parentMatched.isNotEmpty() || matchResult.parentUnmatched.isNotEmpty()

        return FileSpec.builder(target.packageName, fileName)
            .addFileComment("Generated code from @AutoConvert! Do not modify.")
            .addFunction(buildMainFunction(funcName, sourceClass, targetClass, converterClass, matchResult, hasParent))
            .apply {
                if (hasParent) {
                    addFunction(buildParentFunction(sourceClass, targetClass, matchResult))
                }
            }
            .build()
    }

    private fun buildMainFunction(
        funcName: String,
        sourceClass: ClassName,
        targetClass: ClassName,
        converterClass: ClassName,
        matchResult: ConvertPropertyMatch,
        hasParent: Boolean
    ): FunSpec {
        return FunSpec.builder(funcName)
            .receiver(sourceClass)
            .returns(targetClass)
            .addParameter(
                ParameterSpec.builder("converter", converterClass)
                    .defaultValue("%T()", converterClass)
                    .build()
            )
            .addCode(buildCodeBlock {
                addStatement("val target = %T()", targetClass)
                addStatement("converter.onStart(this, target)")
                addStatement("")

                if (hasParent) {
                    addStatement("convertParentFields(this, target)")
                    addStatement("")
                }

                // 当前类属性赋值
                for (prop in matchResult.matched) {
                    addStatement(prop.writeTemplate.replace("{value}", prop.readExpr))
                }

                // 当前类未匹配注释
                appendUnmatchedComments(this, matchResult.unmatched, matchResult.ignored)

                addStatement("")
                addStatement("converter.onEnd(this, target)")
                addStatement("return target")
            })
            .build()
    }

    private fun buildParentFunction(
        sourceClass: ClassName,
        targetClass: ClassName,
        matchResult: ConvertPropertyMatch
    ): FunSpec {
        return FunSpec.builder("convertParentFields")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("source", sourceClass)
            .addParameter("target", targetClass)
            .addCode(buildCodeBlock {
                for (prop in matchResult.parentMatched) {
                    addStatement(prop.writeTemplate.replace("{value}", prop.readExpr))
                }

                // 父类未匹配注释
                if (matchResult.parentUnmatched.isNotEmpty()) {
                    addStatement("")
                    addStatement("// 以下父类目标属性未能自动映射：")
                    for (prop in matchResult.parentUnmatched) {
                        addStatement("// - ${prop.name}: ${prop.typeName}（${prop.reason}）")
                    }
                }
            })
            .build()
    }

    private fun appendUnmatchedComments(
        builder: com.squareup.kotlinpoet.CodeBlock.Builder,
        unmatched: List<UnmatchedProperty>,
        ignored: List<String>
    ) {
        if (unmatched.isEmpty() && ignored.isEmpty()) return

        builder.addStatement("")
        if (unmatched.isNotEmpty()) {
            builder.addStatement("// 以下目标属性在源类中未找到匹配：")
            for (prop in unmatched) {
                builder.addStatement("// - ${prop.name}: ${prop.typeName}（${prop.reason}）")
            }
        }
        if (ignored.isNotEmpty()) {
            builder.addStatement("// 以下目标属性已通过 @AutoConvert.ignoreTargets 忽略：")
            for (name in ignored) {
                builder.addStatement("// - $name")
            }
        }
    }
}
