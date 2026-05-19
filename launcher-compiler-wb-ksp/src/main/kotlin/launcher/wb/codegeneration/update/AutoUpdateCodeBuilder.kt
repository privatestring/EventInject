package launcher.wb.codegeneration.update

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.buildCodeBlock

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/18
 *
 * 代码构建器：根据收集到的属性信息生成 FileSpec。
 */
class AutoUpdateCodeBuilder {

    fun buildFileSpec(target: AutoUpdateTarget, properties: List<PropertyInfo>): FileSpec {
        val simpleName = target.classDecl.simpleName.asString()
        val qualifiedName = target.classDecl.qualifiedName?.asString()
            ?: error("Cannot resolve qualified name for $simpleName")

        val funcName = target.functionName.ifEmpty { "update${simpleName}Fields" }
        val fileName = "${simpleName}AutoUpdate"
        val targetClass = ClassName.bestGuess(qualifiedName)

        val builder = FileSpec.builder(target.packageName, fileName)
            .addFileComment("Generated code from @AutoUpdate! Do not modify.")

        // 添加 stringCheck 所需的 import
        if (target.stringCheckImport.isNotEmpty()) {
            val lastDot = target.stringCheckImport.lastIndexOf('.')
            if (lastDot > 0) {
                val pkg = target.stringCheckImport.substring(0, lastDot)
                val name = target.stringCheckImport.substring(lastDot + 1)
                builder.addImport(pkg, name)
            }
        }

        // 添加父类 update 扩展函数的 import
        if (!target.parentClassName.isNullOrEmpty()) {
            val parentPkg = target.parentClassName.substringBeforeLast(".")
            val parentSimpleName = target.parentClassName.substringAfterLast(".")
            val parentFuncName = "update${parentSimpleName}Fields"
            builder.addImport(parentPkg, parentFuncName)
        }

        builder.addFunction(
            buildUpdateFunction(funcName, targetClass, properties, target.parentClassName, target.stringCheck)
        )

        return builder.build()
    }

    private fun buildUpdateFunction(
        funcName: String,
        targetClass: ClassName,
        properties: List<PropertyInfo>,
        parentClassName: String?,
        stringCheck: String
    ): FunSpec {
        val hasParent = !parentClassName.isNullOrEmpty()

        val builder = FunSpec.builder(funcName)
            .receiver(targetClass)
            .addParameter("from", targetClass)

        if (hasParent) {
            builder.addParameter(
                ParameterSpec.builder("callParent", Boolean::class)
                    .defaultValue("true")
                    .build()
            )
        }

        builder.addCode(buildCodeBlock {
            if (hasParent) {
                val parentSimpleName = parentClassName!!.substringAfterLast(".")
                val parentFuncName = "update${parentSimpleName}Fields"
                beginControlFlow("if (callParent)")
                addStatement("$parentFuncName(from)")
                endControlFlow()
                addStatement("")
            }

            val sorted = properties.sortedBy { it.name }
            for (prop in sorted) {
                if (prop.type == FieldType.SKIP) continue
                addStatement(generateAssignment(prop, stringCheck))
            }
        })

        return builder.build()
    }

    private fun generateAssignment(prop: PropertyInfo, stringCheck: String): String {
        val name = prop.accessName
        return when (prop.type) {
            FieldType.STRING -> {
                val condition = stringCheck.replace("{field}", "from.$name")
                "if ($condition) $name = from.$name"
            }
            FieldType.INT -> "if (from.$name != 0) $name = from.$name"
            FieldType.LONG -> "if (from.$name != 0L) $name = from.$name"
            FieldType.NULLABLE_OBJECT -> "if (from.$name != null) $name = from.$name"
            FieldType.SKIP -> "// ignore $name"
        }
    }
}
