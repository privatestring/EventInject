package launcher.param

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import launcher.Boom
import launcher.classbinding.KnownClassType
import launcher.error.Errors
import launcher.utils.FIELD_NAME_END
import launcher.utils.toTypeName

/**
 * KSP 版参数工厂，解析 @Boom 注解的属性
 */
class ArgumentFactory(
    private val enclosingClass: KSClassDeclaration,
    private val logger: KSPLogger
) {

    fun parseArgument(
        property: KSPropertyDeclaration,
        packageName: String,
        knownClassType: KnownClassType
    ): ArgumentBinding? {
        val ksType = property.type.resolve()
        val paramType = ParamType.fromType(ksType)

        val error = getFieldError(property, knownClassType, paramType)
        if (error != null) {
            logger.error("@Boom ${enclosingClass.qualifiedName?.asString()} $error (${property.simpleName.asString()})", property)
            return null
        }
        paramType!!

        val name = property.simpleName.asString()
        val boomAnnotation = property.annotations.firstOrNull {
            it.shortName.asString() == Boom::class.simpleName
        }

        val index = boomAnnotation?.arguments?.firstOrNull { it.name?.asString() == "index" }?.value as? Int ?: 0
        val keyFromAnnotation = boomAnnotation?.arguments?.firstOrNull { it.name?.asString() == "key" }?.value as? String ?: ""
        val isOptional = boomAnnotation?.arguments?.firstOrNull { it.name?.asString() == "isOptional" }?.value as? Boolean ?: false
        val useFieldKey = boomAnnotation?.arguments?.firstOrNull { it.name?.asString() == "useFieldKey" }?.value as? Boolean ?: false
        val desc = boomAnnotation?.arguments?.firstOrNull { it.name?.asString() == "desc" }?.value as? String ?: ""

        val defaultKey = "$packageName.${name}${FIELD_NAME_END}"
        val key: String = when {
            keyFromAnnotation.isNotEmpty() -> keyFromAnnotation
            useFieldKey -> name
            else -> defaultKey
        }

        val typeName: TypeName = ksType.toTypeName()
        val accessor = FieldAccessor(property, enclosingClass)

        // 收集非 @Boom 和非 @NotNull 的注解
        val annotationList = property.annotations
            .filter { it.shortName.asString() != Boom::class.simpleName && !it.shortName.asString().contains("NotNull") }
            .mapNotNull { it.annotationType.resolve().declaration.qualifiedName?.asString() }
            .toMutableList()

        // KSP 源码层面看不到 Kotlin 编译器隐式生成的 @Nullable 注解，需要根据类型 nullability 手动添加
        val isPrimitiveType = paramType.isPrimitive()
        if (ksType.isMarkedNullable && !isPrimitiveType) {
            annotationList.add("org.jetbrains.annotations.Nullable")
        }

        return ArgumentBinding(name, key, paramType, typeName, index, isOptional, accessor, annotationList, desc)
    }

    private fun getFieldError(
        property: KSPropertyDeclaration,
        knownClassType: KnownClassType,
        paramType: ParamType?
    ): String? = when {
        enclosingClass.classKind != ClassKind.CLASS -> Errors.notAClass
        enclosingClass.modifiers.contains(Modifier.PRIVATE) -> Errors.privateClass
        paramType == null -> Errors.notSupportedType
        !FieldAccessor(property, enclosingClass).isAccessible() -> Errors.inaccessibleField
        paramType.typeUsedBySupertype() && knownClassType == KnownClassType.BroadcastReceiver -> Errors.notBasicTypeInReceiver
        else -> null
    }
}
