package launcher.wb.codegeneration.convert

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/19
 *
 * 属性匹配器。
 *
 * 规则：
 * - 源类可读 = public 字段 或 有 getter 的 private 字段
 * - 目标类可写 = public 可写字段 或 有 setter 的 private 字段
 * - 同名同类型 → 自动赋值
 * - 不匹配 → 注释提醒
 */
class ConvertPropertyMatcher(private val logger: KSPLogger) {

    fun match(
        sourceDecl: KSClassDeclaration,
        targetDecl: KSClassDeclaration,
        ignoreTargets: Set<String>
    ): ConvertPropertyMatch {
        val sourceReadable = collectReadable(sourceDecl)
        val targetOwnWritable = collectWritableOwn(targetDecl)
        val targetParentWritable = collectWritableParent(targetDecl, targetOwnWritable)
        val targetParentReadOnly = collectParentReadOnly(targetDecl, targetOwnWritable, targetParentWritable)

        val matched = mutableListOf<MatchedProperty>()
        val parentMatched = mutableListOf<MatchedProperty>()
        val unmatched = mutableListOf<UnmatchedProperty>()
        val parentUnmatched = mutableListOf<UnmatchedProperty>()
        val ignored = mutableListOf<String>()

        // 当前类
        for ((name, target) in targetOwnWritable) {
            if (name in ignoreTargets) { ignored += name; continue }
            tryMatch(name, target, sourceReadable, "this", "target", matched, unmatched)
        }

        // 父类
        for ((name, target) in targetParentWritable) {
            if (name in ignoreTargets) { ignored += name; continue }
            if (name in targetOwnWritable) continue
            tryMatch(name, target, sourceReadable, "source", "target", parentMatched, parentUnmatched)
        }

        // 父类只读（有字段但无 setter）
        for ((name, info) in targetParentReadOnly) {
            if (name in ignoreTargets) { ignored += name }
            else { parentUnmatched += UnmatchedProperty(name, info.shortTypeName, "目标类无 setter（只读）") }
        }

        return ConvertPropertyMatch(
            matched = matched.sortedBy { it.name },
            parentMatched = parentMatched.sortedBy { it.name },
            unmatched = unmatched.sortedBy { it.name },
            parentUnmatched = parentUnmatched.sortedBy { it.name },
            ignored = ignored.sorted()
        )
    }

    // ==================== 匹配逻辑 ====================

    private fun tryMatch(
        name: String,
        target: FieldInfo,
        sourceReadable: Map<String, FieldInfo>,
        sourcePrefix: String,
        targetPrefix: String,
        matchedList: MutableList<MatchedProperty>,
        unmatchedList: MutableList<UnmatchedProperty>
    ) {
        val source = sourceReadable[name]
        if (source == null) {
            unmatchedList += UnmatchedProperty(name, target.shortTypeName, "源类无同名属性")
            return
        }
        if (!isTypeCompatible(source.type, target.type)) {
            unmatchedList += UnmatchedProperty(name, target.shortTypeName, "源类类型不匹配：${source.shortTypeName}")
            return
        }
        val readExpr = source.buildReadExpr(sourcePrefix)
        val writeTemplate = target.buildWriteTemplate(targetPrefix)
        matchedList += MatchedProperty(name, readExpr, writeTemplate)
    }

    // ==================== 源类：可读属性 ====================

    private fun collectReadable(classDecl: KSClassDeclaration): Map<String, FieldInfo> {
        val result = mutableMapOf<String, FieldInfo>()

        // public 属性（含继承）
        for (prop in classDecl.getAllProperties()) {
            val name = prop.simpleName.asString()
            if (shouldSkipProperty(prop.modifiers)) continue
            if (prop.modifiers.contains(Modifier.PRIVATE)) continue
            val type = prop.type.resolve()
            val typeName = type.declaration.qualifiedName?.asString() ?: continue
            result[name] = FieldInfo(name, type, typeName, accessMethod = null)
        }

        // 继承链上 Java getter 推断的 private 字段
        visitHierarchy(classDecl) { javaClass ->
            for (func in javaClass.getDeclaredFunctions()) {
                val funcName = func.simpleName.asString()
                val fieldName = extractGetterFieldName(funcName) ?: continue
                if (func.parameters.isNotEmpty()) continue
                if (fieldName in result) continue
                val returnType = func.returnType?.resolve() ?: continue
                val typeName = returnType.declaration.qualifiedName?.asString() ?: continue
                result[fieldName] = FieldInfo(fieldName, returnType, typeName, accessMethod = funcName)
            }
        }

        return result
    }

    // ==================== 目标类：可写属性 ====================

    /** 当前类自身声明的可写属性 */
    private fun collectWritableOwn(classDecl: KSClassDeclaration): Map<String, FieldInfo> {
        val result = mutableMapOf<String, FieldInfo>()

        for (prop in classDecl.getDeclaredProperties()) {
            val name = prop.simpleName.asString()
            if (shouldSkipProperty(prop.modifiers)) continue
            if (prop.modifiers.contains(Modifier.PRIVATE)) continue
            if (!prop.isMutable && !isJavaOrigin(classDecl)) continue
            val type = prop.type.resolve()
            val typeName = type.declaration.qualifiedName?.asString() ?: continue
            result[name] = FieldInfo(name, type, typeName, accessMethod = null)
        }

        // 当前类如果是 Java，扫描自身 setter
        if (isJavaOrigin(classDecl)) {
            for (func in classDecl.getDeclaredFunctions()) {
                val funcName = func.simpleName.asString()
                val fieldName = extractSetterFieldName(funcName) ?: continue
                if (func.parameters.size != 1) continue
                if (fieldName in result) continue
                val paramType = func.parameters.first().type.resolve()
                val typeName = paramType.declaration.qualifiedName?.asString() ?: continue
                result[fieldName] = FieldInfo(fieldName, paramType, typeName, accessMethod = funcName)
            }
        }

        return result
    }

    /** 父类链上的可写属性（不含当前类） */
    private fun collectWritableParent(classDecl: KSClassDeclaration, ownProps: Map<String, FieldInfo>): Map<String, FieldInfo> {
        val result = mutableMapOf<String, FieldInfo>()

        // 继承的 public 可写属性
        for (prop in classDecl.getAllProperties()) {
            val name = prop.simpleName.asString()
            if (name in ownProps) continue
            if (shouldSkipProperty(prop.modifiers)) continue
            if (prop.modifiers.contains(Modifier.PRIVATE)) continue
            if (!prop.isMutable && !isJavaOrigin(classDecl)) continue
            val type = prop.type.resolve()
            val typeName = type.declaration.qualifiedName?.asString() ?: continue
            result[name] = FieldInfo(name, type, typeName, accessMethod = null)
        }

        // 父类链上 Java setter 推断的 private 字段
        visitHierarchy(classDecl, skipSelf = true) { javaClass ->
            for (func in javaClass.getDeclaredFunctions()) {
                val funcName = func.simpleName.asString()
                val fieldName = extractSetterFieldName(funcName) ?: continue
                if (func.parameters.size != 1) continue
                if (fieldName in result || fieldName in ownProps) continue
                val paramType = func.parameters.first().type.resolve()
                val typeName = paramType.declaration.qualifiedName?.asString() ?: continue
                result[fieldName] = FieldInfo(fieldName, paramType, typeName, accessMethod = funcName)
            }
        }

        return result
    }

    /** 父类中 private 且无 setter 的字段（只读） */
    private fun collectParentReadOnly(
        classDecl: KSClassDeclaration,
        ownProps: Map<String, FieldInfo>,
        parentWritable: Map<String, FieldInfo>
    ): Map<String, FieldInfo> {
        val result = mutableMapOf<String, FieldInfo>()

        visitHierarchy(classDecl, skipSelf = true) { javaClass ->
            for (prop in javaClass.getDeclaredProperties()) {
                val name = prop.simpleName.asString()
                if (name in ownProps || name in parentWritable || name in result) continue
                if (prop.modifiers.contains(Modifier.JAVA_STATIC)) continue
                if (!prop.modifiers.contains(Modifier.PRIVATE)) continue
                val type = prop.type.resolve()
                val typeName = type.declaration.qualifiedName?.asString() ?: continue
                result[name] = FieldInfo(name, type, typeName, accessMethod = null)
            }
        }

        return result
    }

    // ==================== 继承链遍历 ====================

    /** 遍历继承链上的 Java 类，执行 action */
    private inline fun visitHierarchy(
        classDecl: KSClassDeclaration,
        skipSelf: Boolean = false,
        action: (KSClassDeclaration) -> Unit
    ) {
        var current: KSClassDeclaration? = if (skipSelf) findSuperClass(classDecl) else classDecl
        while (current != null) {
            val qName = current.qualifiedName?.asString()
            if (qName == "java.lang.Object" || qName == "kotlin.Any") break
            if (isJavaOrigin(current)) action(current)
            current = findSuperClass(current)
        }
    }

    private fun findSuperClass(classDecl: KSClassDeclaration): KSClassDeclaration? {
        for (superType in classDecl.superTypes) {
            val decl = superType.resolve().declaration as? KSClassDeclaration ?: continue
            val name = decl.qualifiedName?.asString() ?: continue
            if (name == "kotlin.Any" || name == "java.lang.Object") continue
            if (decl.classKind == ClassKind.INTERFACE) continue
            return decl
        }
        return null
    }

    // ==================== 工具方法 ====================

    private fun isJavaOrigin(decl: KSClassDeclaration) =
        decl.origin == Origin.JAVA || decl.origin == Origin.JAVA_LIB

    private fun shouldSkipProperty(modifiers: Set<Modifier>) =
        modifiers.contains(Modifier.JAVA_STATIC) || modifiers.contains(Modifier.CONST)

    private fun extractGetterFieldName(funcName: String): String? = when {
        funcName.startsWith("get") && funcName.length > 3 -> {
            val rest = funcName.substring(3)
            if (rest[0].isUpperCase()) rest[0].lowercase() + rest.substring(1) else rest
        }
        funcName.startsWith("is") && funcName.length > 2 && funcName[2].isUpperCase() -> funcName
        else -> null
    }

    private fun extractSetterFieldName(funcName: String): String? = when {
        funcName.startsWith("set") && funcName.length > 3 -> {
            val rest = funcName.substring(3)
            if (rest[0].isUpperCase()) rest[0].lowercase() + rest.substring(1) else rest
        }
        else -> null
    }

    private fun isTypeCompatible(sourceType: KSType, targetType: KSType): Boolean {
        val sourceName = sourceType.declaration.qualifiedName?.asString()
        val targetName = targetType.declaration.qualifiedName?.asString()

        if (sourceName == targetName) {
            return !(sourceType.isMarkedNullable && !targetType.isMarkedNullable)
        }
        if (normalizeType(sourceName) == normalizeType(targetName)) return true
        return targetType.isAssignableFrom(sourceType)
    }

    private fun normalizeType(name: String?) = when (name) {
        "java.lang.String" -> "kotlin.String"
        "java.lang.Integer", "int" -> "kotlin.Int"
        "java.lang.Long", "long" -> "kotlin.Long"
        "java.lang.Double", "double" -> "kotlin.Double"
        "java.lang.Float", "float" -> "kotlin.Float"
        "java.lang.Boolean", "boolean" -> "kotlin.Boolean"
        else -> name
    }

    // ==================== 内部数据类 ====================

    private data class FieldInfo(
        val name: String,
        val type: KSType,
        val qualifiedTypeName: String,
        /** getter/setter 方法名，null 表示直接字段访问 */
        val accessMethod: String?
    ) {
        val shortTypeName get() = qualifiedTypeName.substringAfterLast(".")

        /** 生成读取表达式 */
        fun buildReadExpr(prefix: String) =
            if (accessMethod != null) "$prefix.$accessMethod()" else "$prefix.$name"

        /** 生成写入模板，{value} 为值占位符 */
        fun buildWriteTemplate(prefix: String) =
            if (accessMethod != null) "$prefix.$accessMethod({value})" else "$prefix.$name = {value}"
    }
}
