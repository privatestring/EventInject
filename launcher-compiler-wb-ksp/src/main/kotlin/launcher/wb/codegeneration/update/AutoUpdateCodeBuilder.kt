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

    fun buildFileSpec(target: AutoUpdateTarget, properties: List<PropertyInfo>, copyProperties: List<PropertyInfo> = emptyList()): FileSpec {
        val simpleName = target.classDecl.simpleName.asString()
        val qualifiedName = target.classDecl.qualifiedName?.asString()
            ?: error("Cannot resolve qualified name for $simpleName")

        val funcName = target.functionName.ifEmpty { "update${simpleName}Fields" }
        val fileName = "${simpleName}AutoUpdate"
        val targetClass = ClassName.bestGuess(qualifiedName)

        val builder = FileSpec.builder(target.packageName, fileName)
            .addFileComment("Generated code from @AutoUpdate! Do not modify.")

        // 检查是否有 String 字段使用类级别 stringCheck（未被 @AutoUpdateCheck 覆盖）
        val hasClassLevelStringCheck = properties.any {
            it.type == FieldType.STRING && it.customCheck == null
        }

        // 仅当有字段实际使用类级别 stringCheck 时才 import
        if (hasClassLevelStringCheck && target.stringCheckImport.isNotEmpty()) {
            addImportIfValid(builder, target.stringCheckImport)
        }

        // 添加属性级别 @AutoUpdateCheck 的 import
        for (prop in properties) {
            val importPath = prop.customCheckImport ?: continue
            addImportIfValid(builder, importPath)
        }

        // 添加父类 update 扩展函数的 import
        if (!target.parentClassName.isNullOrEmpty()) {
            val parentPkg = target.parentClassName.substringBeforeLast(".")
            val parentSimpleName = target.parentClassName.substringAfterLast(".")
            val parentFuncName = resolveParentFuncName(target.parentFunctionName, parentSimpleName)
            builder.addImport(parentPkg, parentFuncName)
            // copy 函数也需要 import 父类的 copy
            if (target.generateCopy) {
                builder.addImport(parentPkg, "copy${parentSimpleName}Fields")
            }
        }

        builder.addFunction(
            buildUpdateFunction(funcName, targetClass, properties, target.parentClassName, target.parentFunctionName, target.stringCheck)
        )

        // 生成 copy 函数（无条件全量赋值）
        if (target.generateCopy) {
            val copyFuncName = "copy${simpleName}Fields"
            builder.addFunction(
                buildCopyFunction(copyFuncName, targetClass, copyProperties, target.parentClassName, target.parentFunctionName)
            )
        }

        return builder.build()
    }

    /**
     * 解析父类实际的 update 函数名。
     * 如果父类指定了 functionName 则使用，否则按默认规则拼接。
     */
    private fun resolveParentFuncName(parentFunctionName: String?, parentSimpleName: String): String {
        return if (!parentFunctionName.isNullOrEmpty()) parentFunctionName
        else "update${parentSimpleName}Fields"
    }

    private fun addImportIfValid(builder: FileSpec.Builder, importPath: String) {
        val lastDot = importPath.lastIndexOf('.')
        if (lastDot > 0) {
            val pkg = importPath.substring(0, lastDot)
            val name = importPath.substring(lastDot + 1)
            builder.addImport(pkg, name)
        }
    }

    private fun buildUpdateFunction(
        funcName: String,
        targetClass: ClassName,
        properties: List<PropertyInfo>,
        parentClassName: String?,
        parentFunctionName: String?,
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
                val parentFuncName = resolveParentFuncName(parentFunctionName, parentSimpleName)
                beginControlFlow("if (callParent)")
                addStatement("$parentFuncName(from)")
                endControlFlow()
                addStatement("")
            }

            val sorted = properties.sortedBy { it.name }
            val active = sorted.filter { !it.ignored }
            val ignored = sorted.filter { it.ignored }

            for (prop in active) {
                addStatement(generateAssignment(prop, stringCheck))
            }

            // 生成被忽略字段的注释
            if (ignored.isNotEmpty()) {
                addStatement("")
                for (prop in ignored) {
                    addStatement("// ${prop.ignoreReason}: ${prop.name}")
                }
            }
        })

        return builder.build()
    }

    private fun generateAssignment(prop: PropertyInfo, stringCheck: String): String {
        val name = prop.accessName

        // @AutoUpdateAlways: 无条件赋值
        if (prop.alwaysUpdate) {
            return "$name = from.$name"
        }

        // 属性级别 @AutoUpdateCheck 优先，适用于所有类型（包括 SKIP）
        if (prop.customCheck != null) {
            val condition = prop.customCheck
                .replace("{field}", "from.$name")
                .replace("{from}", "from")
            return "if ($condition) $name = from.$name"
        }

        return when (prop.type) {
            FieldType.STRING -> {
                val condition = stringCheck.replace("{field}", "from.$name")
                "if ($condition) $name = from.$name"
            }
            FieldType.INT -> "if (from.$name != 0) $name = from.$name"
            FieldType.LONG -> "if (from.$name != 0L) $name = from.$name"
            FieldType.DOUBLE -> "if (from.$name != 0.0) $name = from.$name"
            FieldType.FLOAT -> "if (from.$name != 0.0f) $name = from.$name"
            FieldType.NULLABLE_OBJECT -> "if (from.$name != null) $name = from.$name"
            FieldType.OBJECT -> "$name = from.$name"
            FieldType.SKIP -> error("SKIP type should not reach generateAssignment")
        }
    }

    /**
     * 生成无条件全量拷贝函数。所有字段直接赋值，不做任何检查。
     * 忽略 @AutoUpdateIgnore（因为 copy 是全量拷贝语义）。
     */
    private fun buildCopyFunction(
        funcName: String,
        targetClass: ClassName,
        properties: List<PropertyInfo>,
        parentClassName: String?,
        parentFunctionName: String?
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
                val parentCopyFuncName = "copy${parentSimpleName}Fields"
                beginControlFlow("if (callParent)")
                addStatement("$parentCopyFuncName(from)")
                endControlFlow()
                addStatement("")
            }

            val sorted = properties.sortedBy { it.name }
            for (prop in sorted) {
                addStatement("${prop.accessName} = from.${prop.accessName}")
            }
        })

        return builder.build()
    }
}
