package launcher.wb.mapper.codegen

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import launcher.wb.mapper.MapperMethodDescriptor

/**
 * 类型解析与判断工具类
 */
object TypeResolver {

    fun isPrimitiveType(type: KSType): Boolean {
        val name = type.declaration.qualifiedName?.asString() ?: return false
        return name in PRIMITIVE_TYPES
    }

    fun isPrimitiveNumericType(name: String): Boolean {
        return name in PRIMITIVE_NUMERIC_TYPES
    }

    fun isTypeCompatible(type1: KSType, type2: KSType): Boolean {
        val decl1 = type1.declaration.qualifiedName?.asString() ?: return false
        val decl2 = type2.declaration.qualifiedName?.asString() ?: return false
        return decl1 == decl2
    }

    fun isAssignable(source: KSType?, target: KSType): Boolean {
        if (source == null) return true

        val sourceQName = source.declaration.qualifiedName?.asString() ?: return false
        val targetQName = target.declaration.qualifiedName?.asString() ?: return false

        // 相同类型（非集合时直接返回 true）
        if (sourceQName == targetQName && !isCollectionType(source)) return true

        // 基本类型与包装类型兼容
        if (isPrimitiveBoxingMatch(sourceQName, targetQName)) return true

        // 集合类型兼容性：必须比较元素类型
        if (isCollectionType(source) && isCollectionType(target)) {
            val sourceElement = getCollectionElementType(source)
            val targetElement = getCollectionElementType(target)
            if (sourceElement != null && targetElement != null) {
                return isAssignable(sourceElement, targetElement)
            }
            return sourceQName == targetQName
        }

        // 相同类型
        if (sourceQName == targetQName) return true

        // 检查继承关系
        val sourceDecl = source.declaration as? KSClassDeclaration ?: return false
        return sourceDecl.getAllSuperTypes().any { superType ->
            superType.declaration.qualifiedName?.asString() == targetQName
        }
    }

    fun isSameType(type1: KSType, type2: KSType): Boolean {
        return type1.declaration.qualifiedName?.asString() == type2.declaration.qualifiedName?.asString()
    }

    fun isCollectionType(type: KSType?): Boolean {
        if (type == null) return false
        val name = type.declaration.qualifiedName?.asString() ?: return false
        return name in COLLECTION_TYPES
    }

    fun getCollectionElementType(type: KSType?): KSType? {
        if (type == null) return null
        val typeArgs = type.arguments
        if (typeArgs.isEmpty()) return null
        return typeArgs.first().type?.resolve()
    }

    fun getCollectionTypeName(type: KSType?): String? {
        if (type == null) return null
        val name = type.declaration.qualifiedName?.asString() ?: return null
        return when {
            name == "java.util.ArrayList" || name == "kotlin.collections.ArrayList" -> "java.util.ArrayList"
            name == "java.util.LinkedList" -> "java.util.LinkedList"
            name == "java.util.HashSet" || name == "kotlin.collections.HashSet" -> "java.util.HashSet"
            name == "java.util.LinkedHashSet" || name == "kotlin.collections.LinkedHashSet" -> "java.util.LinkedHashSet"
            name == "java.util.List" || name == "kotlin.collections.List" || name == "kotlin.collections.MutableList" -> "java.util.ArrayList"
            name == "java.util.Set" || name == "kotlin.collections.Set" || name == "kotlin.collections.MutableSet" -> "java.util.HashSet"
            name == "java.util.Collection" || name == "kotlin.collections.Collection" -> "java.util.ArrayList"
            else -> null
        }
    }

    /**
     * 将 KSType 转换为 JavaPoet TypeName
     */
    fun resolveTypeName(type: KSType?): TypeName {
        if (type == null) return TypeName.VOID
        val qualifiedName = type.declaration.qualifiedName?.asString() ?: return TypeName.OBJECT

        // 处理基本类型：nullable 用包装类，non-null 用原始类型
        if (type.isMarkedNullable) {
            val boxedType = KOTLIN_TO_JAVA_BOXED[qualifiedName]
            if (boxedType != null) return boxedType
        } else {
            val primitiveTypeName = KOTLIN_TO_JAVA_TYPE[qualifiedName]
            if (primitiveTypeName != null) return primitiveTypeName
        }

        // 处理集合类型（带泛型）
        val typeArgs = type.arguments
        if (typeArgs.isNotEmpty() && isCollectionType(type)) {
            val javaCollectionName = when (qualifiedName) {
                "kotlin.collections.List", "kotlin.collections.MutableList" -> "java.util.List"
                "kotlin.collections.Set", "kotlin.collections.MutableSet" -> "java.util.Set"
                "kotlin.collections.ArrayList" -> "java.util.ArrayList"
                "kotlin.collections.Map", "kotlin.collections.MutableMap" -> "java.util.Map"
                else -> qualifiedName
            }
            val rawType = ClassName.bestGuess(javaCollectionName)
            val elementType = typeArgs.first().type?.resolve()
            if (elementType != null) {
                val elementTypeName = resolveTypeName(elementType)
                return ParameterizedTypeName.get(rawType, elementTypeName)
            }
            return rawType
        }

        // 处理普通类型
        return ClassName.bestGuess(qualifiedName)
    }

    /**
     * 查找 Mapper 中是否有元素类型的映射方法
     */
    fun findElementMapperMethod(
        sourceType: KSType?,
        targetType: KSType?,
        methods: List<MapperMethodDescriptor>
    ): MapperMethodDescriptor? {
        if (sourceType == null || targetType == null) return null
        return methods.firstOrNull { method ->
            val sourceParam = method.parameters.firstOrNull { !it.isMappingTarget }
            val hasMappingTarget = method.mappingTarget != null

            when {
                hasMappingTarget -> {
                    val mappingTargetType = method.mappingTarget?.type ?: return@firstOrNull false
                    if (sourceParam == null) return@firstOrNull false
                    isTypeCompatible(mappingTargetType, targetType) && isTypeCompatible(sourceParam.type, sourceType)
                }
                else -> {
                    if (sourceParam == null || method.parameters.size != 1) return@firstOrNull false
                    isTypeCompatible(method.returnType, targetType) && isTypeCompatible(sourceParam.type, sourceType)
                }
            }
        }
    }

    fun generateUniqueName(base: String, used: MutableSet<String>): String {
        var candidate = base
        var index = 0
        while (used.contains(candidate)) {
            index++
            candidate = base + index
        }
        return candidate
    }

    private fun isPrimitiveBoxingMatch(name1: String, name2: String): Boolean {
        val mapping = mapOf(
            "kotlin.Int" to "java.lang.Integer",
            "kotlin.Long" to "java.lang.Long",
            "kotlin.Double" to "java.lang.Double",
            "kotlin.Float" to "java.lang.Float",
            "kotlin.Boolean" to "java.lang.Boolean",
            "kotlin.Byte" to "java.lang.Byte",
            "kotlin.Short" to "java.lang.Short",
            "kotlin.Char" to "java.lang.Character",
            "int" to "java.lang.Integer",
            "long" to "java.lang.Long",
            "double" to "java.lang.Double",
            "float" to "java.lang.Float",
            "boolean" to "java.lang.Boolean",
            "byte" to "java.lang.Byte",
            "short" to "java.lang.Short",
            "char" to "java.lang.Character"
        )
        return mapping[name1] == name2 || mapping[name2] == name1 ||
                mapping.entries.any { (k, v) -> (k == name1 && v == name2) || (k == name2 && v == name1) }
    }

    private val PRIMITIVE_TYPES = setOf(
        "kotlin.Byte", "kotlin.Short", "kotlin.Int", "kotlin.Long",
        "kotlin.Float", "kotlin.Double", "kotlin.Char", "kotlin.Boolean",
        "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
        "java.lang.Float", "java.lang.Double", "java.lang.Character", "java.lang.Boolean",
        "int", "long", "double", "float", "boolean", "byte", "short", "char"
    )

    private val PRIMITIVE_NUMERIC_TYPES = setOf(
        "kotlin.Byte", "kotlin.Short", "kotlin.Int", "kotlin.Long",
        "kotlin.Float", "kotlin.Double", "kotlin.Char",
        "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
        "java.lang.Float", "java.lang.Double", "java.lang.Character"
    )

    private val COLLECTION_TYPES = setOf(
        "java.util.List", "java.util.Set", "java.util.Collection",
        "java.util.ArrayList", "java.util.LinkedList",
        "java.util.HashSet", "java.util.LinkedHashSet",
        "kotlin.collections.List", "kotlin.collections.MutableList",
        "kotlin.collections.Set", "kotlin.collections.MutableSet",
        "kotlin.collections.ArrayList", "kotlin.collections.HashSet",
        "kotlin.collections.LinkedHashSet", "kotlin.collections.Collection"
    )

    private val KOTLIN_TO_JAVA_TYPE: Map<String, TypeName> = mapOf(
        "kotlin.Unit" to TypeName.VOID,
        "kotlin.Int" to TypeName.INT,
        "kotlin.Long" to TypeName.LONG,
        "kotlin.Double" to TypeName.DOUBLE,
        "kotlin.Float" to TypeName.FLOAT,
        "kotlin.Boolean" to TypeName.BOOLEAN,
        "kotlin.Byte" to TypeName.BYTE,
        "kotlin.Short" to TypeName.SHORT,
        "kotlin.Char" to TypeName.CHAR,
        "kotlin.String" to ClassName.get("java.lang", "String"),
        "java.lang.String" to ClassName.get("java.lang", "String"),
        "java.lang.Integer" to ClassName.get("java.lang", "Integer"),
        "java.lang.Long" to ClassName.get("java.lang", "Long"),
        "java.lang.Double" to ClassName.get("java.lang", "Double"),
        "java.lang.Float" to ClassName.get("java.lang", "Float"),
        "java.lang.Boolean" to ClassName.get("java.lang", "Boolean"),
        "java.lang.Byte" to ClassName.get("java.lang", "Byte"),
        "java.lang.Short" to ClassName.get("java.lang", "Short"),
        "java.lang.Character" to ClassName.get("java.lang", "Character"),
        "java.lang.Object" to TypeName.OBJECT
    )

    /** Kotlin nullable 基本类型 → Java 包装类 */
    private val KOTLIN_TO_JAVA_BOXED: Map<String, TypeName> = mapOf(
        "kotlin.Int" to ClassName.get("java.lang", "Integer"),
        "kotlin.Long" to ClassName.get("java.lang", "Long"),
        "kotlin.Double" to ClassName.get("java.lang", "Double"),
        "kotlin.Float" to ClassName.get("java.lang", "Float"),
        "kotlin.Boolean" to ClassName.get("java.lang", "Boolean"),
        "kotlin.Byte" to ClassName.get("java.lang", "Byte"),
        "kotlin.Short" to ClassName.get("java.lang", "Short"),
        "kotlin.Char" to ClassName.get("java.lang", "Character"),
        "kotlin.String" to ClassName.get("java.lang", "String")
    )
}
