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
        }

        builder.addFunction(
            buildUpdateFunction(funcName, targetClass, properties, target.parentClassName, target.parentFunctionName, target.stringCheck)
        )

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
            FieldType.INT -> {
                val default = normalizeIntLiteral(prop.defaultValue) ?: "0"
                "if (from.$name != $default) $name = from.$name"
            }
            FieldType.LONG -> {
                val default = normalizeLongLiteral(prop.defaultValue) ?: "0L"
                "if (from.$name != $default) $name = from.$name"
            }
            FieldType.DOUBLE -> {
                val default = normalizeDoubleLiteral(prop.defaultValue) ?: "0.0"
                "if (from.$name != $default) $name = from.$name"
            }
            FieldType.FLOAT -> {
                val default = normalizeFloatLiteral(prop.defaultValue) ?: "0.0f"
                "if (from.$name != $default) $name = from.$name"
            }
            FieldType.BOOLEAN -> {
                val default = prop.defaultValue ?: "false"
                if (default == "false") "if (from.$name) $name = from.$name"
                else "if (!from.$name) $name = from.$name"
            }
            FieldType.NULLABLE_OBJECT -> "if (from.$name != null) $name = from.$name"
            FieldType.OBJECT -> "$name = from.$name"
            FieldType.SKIP -> error("SKIP type should not reach generateAssignment")
        }
    }

    /** 确保 Int 字面量不带类型后缀 */
    private fun normalizeIntLiteral(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val v = value.trimEnd('L', 'l')
        // 验证是合法的整数字面量
        return if (v.toLongOrNull() != null) v else null
    }

    /** 确保 Long 字面量带 L 后缀 */
    private fun normalizeLongLiteral(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val v = value.trimEnd('L', 'l', 'f', 'F')
        // 验证是合法的整数字面量
        return if (v.toLongOrNull() != null) "${v}L" else null
    }

    /** 确保 Double 字面量是浮点格式（不带 f 后缀） */
    private fun normalizeDoubleLiteral(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val v = value.trimEnd('f', 'F', 'L', 'l')
        val num = v.toDoubleOrNull() ?: return null
        // 确保有小数点
        return if ('.' in v) v else "$v.0"
    }

    /** 确保 Float 字面量带 f 后缀 */
    private fun normalizeFloatLiteral(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val v = value.trimEnd('f', 'F')
        val num = v.toDoubleOrNull() ?: return null
        val base = if ('.' in v) v else "$v.0"
        return "${base}f"
    }
}
