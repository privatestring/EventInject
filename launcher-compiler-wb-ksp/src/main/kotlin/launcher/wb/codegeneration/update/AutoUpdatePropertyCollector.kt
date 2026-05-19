package launcher.wb.codegeneration.update

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import launcher.wb.codegeneration.arg
import wb.bean.AutoUpdateAlways
import wb.bean.AutoUpdateCheck
import wb.bean.AutoUpdateIgnore

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/18
 *
 * 属性收集器：从类声明中收集需要生成赋值代码的属性列表。
 * 支持 Kotlin 属性和 Java private 字段（通过 getter/setter 推断）。
 */
class AutoUpdatePropertyCollector(private val logger: KSPLogger) {

    private val ignoreQualifiedName = AutoUpdateIgnore::class.qualifiedName!!
    private val checkQualifiedName = AutoUpdateCheck::class.qualifiedName!!
    private val alwaysQualifiedName = AutoUpdateAlways::class.qualifiedName!!

    /**
     * 收集类声明的属性，过滤掉 @AutoUpdateIgnore、static、companion 等。
     */
    fun collectProperties(classDecl: KSClassDeclaration): List<PropertyInfo> {
        return collectPropertiesInternal(classDecl, forCopy = false)
    }

    /**
     * 收集类声明的所有属性用于 copy 函数（不跳过 SKIP 类型）。
     */
    fun collectAllProperties(classDecl: KSClassDeclaration): List<PropertyInfo> {
        return collectPropertiesInternal(classDecl, forCopy = true)
    }

    private fun collectPropertiesInternal(classDecl: KSClassDeclaration, forCopy: Boolean): List<PropertyInfo> {
        val result = mutableListOf<PropertyInfo>()
        val collectedNames = mutableSetOf<String>()

        // 收集父类所有属性名（用于排除继承的属性）
        val parentPropertyNames = collectParentPropertyNames(classDecl)

        // 第一步：从 getAllProperties() 收集（对 Kotlin 类有效，对 Java public 字段有效）
        for (prop in classDecl.getAllProperties()) {
            val propName = prop.simpleName.asString()
            if (propName in parentPropertyNames) continue
            if (propName in collectedNames) continue

            val info = processProperty(prop, forCopy) ?: continue
            result.add(info)
            collectedNames.add(propName)
        }

        // 第二步：对 Java 类，扫描 getter/setter 方法对推断 private 字段
        if (classDecl.origin == Origin.JAVA || classDecl.origin == Origin.JAVA_LIB) {
            val javaFields = collectJavaPrivateFields(classDecl, parentPropertyNames, collectedNames, forCopy)
            result.addAll(javaFields)
        }

        return result
    }

    /**
     * 收集所有父类的属性名（包括 Java getter/setter 推断的）
     */
    private fun collectParentPropertyNames(classDecl: KSClassDeclaration): Set<String> {
        val names = mutableSetOf<String>()
        var superClass = classDecl.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
        while (superClass != null && superClass.qualifiedName?.asString() != "java.lang.Object") {
            superClass.getAllProperties().forEach { names.add(it.simpleName.asString()) }
            if (superClass.origin == Origin.JAVA || superClass.origin == Origin.JAVA_LIB) {
                for (func in superClass.getDeclaredFunctions()) {
                    extractFieldNameFromGetter(func.simpleName.asString())?.let { names.add(it) }
                }
            }
            superClass = superClass.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
        }
        return names
    }

    /**
     * 处理单个 KSPropertyDeclaration，返回 PropertyInfo 或 null（跳过）
     */
    private fun processProperty(prop: KSPropertyDeclaration, forCopy: Boolean): PropertyInfo? {
        val propName = prop.simpleName.asString()

        if (prop.modifiers.contains(Modifier.JAVA_STATIC)) return null
        if (prop.modifiers.contains(Modifier.CONST)) return null

        val hasIgnore = hasAutoUpdateIgnore(prop.annotations)
        val hasCheck = hasAutoUpdateCheckAnnotation(prop.annotations)
        val hasAlways = hasAutoUpdateAlwaysAnnotation(prop.annotations)

        // 校验：同时标注 @AutoUpdateIgnore 和 @AutoUpdateCheck/@AutoUpdateAlways
        if (hasIgnore && (hasCheck || hasAlways)) {
            logger.warn(
                "@AutoUpdateIgnore and @AutoUpdateCheck/@AutoUpdateAlways are both present on '$propName', " +
                    "@AutoUpdateIgnore takes precedence and the field will be skipped.",
                prop
            )
        }

        // @AutoUpdateIgnore: 标记为忽略（forCopy 模式下直接跳过，不生成注释）
        if (hasIgnore) {
            return if (forCopy) null
            else PropertyInfo(propName, FieldType.SKIP, ignored = true, ignoreReason = "@AutoUpdateIgnore")
        }

        if (prop.setter == null && prop.modifiers.contains(Modifier.PRIVATE) && !hasCheck && !hasAlways && !forCopy) return null
        if (propName.startsWith("_")) return null

        val type = prop.type.resolve()
        val typeName = type.declaration.qualifiedName?.asString() ?: return null
        val isNullable = type.isMarkedNullable

        val fieldType = classifyFieldType(typeName, isNullable)

        // 读取属性级别 @AutoUpdateCheck
        val (customCheck, customCheckImport) = extractAutoUpdateCheck(prop.annotations)

        // SKIP 类型处理
        if (fieldType == FieldType.SKIP && !forCopy && customCheck == null && !hasAlways) {
            // 返回忽略信息用于生成注释
            val reason = "skip(${typeName.substringAfterLast(".")})"
            return PropertyInfo(propName, FieldType.SKIP, ignored = true, ignoreReason = reason)
        }

        // 校验：condition 中没有 {field} 或 {from} 占位符
        if (customCheck != null && !customCheck.contains("{field}") && !customCheck.contains("{from}")) {
            logger.error(
                "@AutoUpdateCheck.condition on '$propName' must contain '{field}' or '{from}' placeholder.",
                prop
            )
            return null
        }

        return PropertyInfo(propName, fieldType, customCheck, customCheckImport, hasAlways)
    }

    /**
     * 扫描 Java 类的 getter/setter 方法对，推断 private 字段。
     */
    private fun collectJavaPrivateFields(
        classDecl: KSClassDeclaration,
        parentPropertyNames: Set<String>,
        alreadyCollected: Set<String>,
        forCopy: Boolean
    ): List<PropertyInfo> {
        val result = mutableListOf<PropertyInfo>()
        val functions = classDecl.getDeclaredFunctions().toList()

        // 收集字段声明上的注解信息（@AutoUpdateIgnore / @AutoUpdateCheck / @AutoUpdateAlways）
        val ignoredFieldNames = mutableSetOf<String>()
        val fieldCheckMap = mutableMapOf<String, Pair<String?, String?>>() // fieldName → (customCheck, customCheckImport)
        val alwaysFieldNames = mutableSetOf<String>()
        // 从所有可见属性中收集注解（getDeclaredProperties + getAllProperties 覆盖 Java private/public 字段）
        val allProps = (classDecl.getDeclaredProperties() + classDecl.getAllProperties())
            .distinctBy { it.simpleName.asString() }
        for (prop in allProps) {
            val fieldName = prop.simpleName.asString()
            if (hasAutoUpdateIgnore(prop.annotations)) {
                ignoredFieldNames.add(fieldName)
            }
            if (hasAutoUpdateAlwaysAnnotation(prop.annotations)) {
                alwaysFieldNames.add(fieldName)
            }
            val checkInfo = extractAutoUpdateCheck(prop.annotations)
            if (checkInfo.first != null) {
                fieldCheckMap[fieldName] = checkInfo
            }
        }

        // 收集所有 getter 方法
        val getters = mutableMapOf<String, KSFunctionDeclaration>()
        for (func in functions) {
            val fieldName = extractFieldNameFromGetter(func.simpleName.asString()) ?: continue
            if (func.parameters.isNotEmpty()) continue
            getters[fieldName] = func
        }

        // 收集所有 setter 方法
        val setters = mutableSetOf<String>()
        for (func in functions) {
            val fieldName = extractFieldNameFromSetter(func.simpleName.asString()) ?: continue
            if (func.parameters.size != 1) continue
            setters.add(fieldName)
        }

        // 找到 getter + setter 配对的字段
        for ((fieldName, getter) in getters) {
            if (fieldName !in setters) continue
            if (fieldName in parentPropertyNames) continue
            if (fieldName in alreadyCollected) continue
            if (fieldName in ignoredFieldNames) continue
            if (hasAutoUpdateIgnore(getter.annotations)) continue

            val returnType = getter.returnType?.resolve() ?: continue
            val typeName = returnType.declaration.qualifiedName?.asString() ?: continue
            val isNullable = returnType.isMarkedNullable

            val fieldType = classifyFieldType(typeName, isNullable)
            val (customCheck, customCheckImport) = fieldCheckMap[fieldName] ?: (null to null)
            val isAlways = fieldName in alwaysFieldNames

            // SKIP 类型：forCopy 模式全部收集；否则需要 @AutoUpdateCheck 或 @AutoUpdateAlways
            if (fieldType == FieldType.SKIP && !forCopy && customCheck == null && !isAlways) continue

            // 校验：condition 中没有 {field} 或 {from} 占位符
            if (customCheck != null && !customCheck.contains("{field}") && !customCheck.contains("{from}")) {
                logger.error(
                    "@AutoUpdateCheck.condition on '$fieldName' must contain '{field}' or '{from}' placeholder.",
                    getter
                )
                continue
            }

            result.add(PropertyInfo(fieldName, fieldType, customCheck, customCheckImport, isAlways))
        }

        return result
    }

    private fun extractFieldNameFromGetter(funcName: String): String? = when {
        funcName.startsWith("get") && funcName.length > 3 && funcName[3].isUpperCase() ->
            funcName[3].lowercase() + funcName.substring(4)
        funcName.startsWith("is") && funcName.length > 2 && funcName[2].isUpperCase() ->
            funcName
        else -> null
    }

    private fun extractFieldNameFromSetter(funcName: String): String? = when {
        funcName.startsWith("set") && funcName.length > 3 && funcName[3].isUpperCase() ->
            funcName[3].lowercase() + funcName.substring(4)
        else -> null
    }

    private fun hasAutoUpdateIgnore(annotations: Sequence<com.google.devtools.ksp.symbol.KSAnnotation>): Boolean {
        return annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == ignoreQualifiedName
        }
    }

    private fun hasAutoUpdateCheckAnnotation(annotations: Sequence<KSAnnotation>): Boolean {
        return annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == checkQualifiedName
        }
    }

    private fun hasAutoUpdateAlwaysAnnotation(annotations: Sequence<KSAnnotation>): Boolean {
        return annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == alwaysQualifiedName
        }
    }

    /**
     * 从属性注解中提取 @AutoUpdateCheck 的 stringCheck 和 stringCheckImport。
     * 返回 Pair(customCheck, customCheckImport)，未标注时返回 (null, null)。
     */
    private fun extractAutoUpdateCheck(annotations: Sequence<KSAnnotation>): Pair<String?, String?> {
        val anno = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == checkQualifiedName
        } ?: return null to null

        val check = anno.arg<String>(AutoUpdateCheck::condition.name)
        val import = anno.arg<String>(AutoUpdateCheck::conditionImport.name)
        return check to import?.takeIf { it.isNotEmpty() }
    }

    companion object {
        fun classifyFieldType(typeName: String, isNullable: Boolean): FieldType = when {
            typeName == "kotlin.String" || typeName == "java.lang.String" -> FieldType.STRING
            (typeName == "kotlin.Int" || typeName == "java.lang.Integer") && !isNullable -> FieldType.INT
            typeName == "int" -> FieldType.INT
            (typeName == "kotlin.Long" || typeName == "java.lang.Long") && !isNullable -> FieldType.LONG
            typeName == "long" -> FieldType.LONG
            (typeName == "kotlin.Double" || typeName == "java.lang.Double") && !isNullable -> FieldType.DOUBLE
            typeName == "double" -> FieldType.DOUBLE
            (typeName == "kotlin.Float" || typeName == "java.lang.Float") && !isNullable -> FieldType.FLOAT
            typeName == "float" -> FieldType.FLOAT
            (typeName == "kotlin.Boolean" && !isNullable) || typeName == "boolean" -> FieldType.SKIP
            typeName == "kotlin.IntArray" || typeName == "int[]" -> FieldType.SKIP
            isNullable -> FieldType.NULLABLE_OBJECT
            else -> FieldType.OBJECT
        }
    }
}
