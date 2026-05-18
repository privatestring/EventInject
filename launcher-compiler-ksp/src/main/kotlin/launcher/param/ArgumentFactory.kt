package launcher.param

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.javapoet.TypeName
import launcher.Boom
import launcher.classbinding.KnownClassType
import launcher.error.Errors
import launcher.utils.FIELD_NAME_END
import launcher.utils.toTypeName

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * 解析 @Boom 注解的属性，构建 [ArgumentBinding]。
 * 负责类型判断、访问性校验、key 生成、注解收集。
 */
class ArgumentFactory(
    private val enclosingClass: KSClassDeclaration,
    private val logger: KSPLogger
) {

    /**
     * 解析单个属性，返回 null 表示校验失败（已输出编译错误）。
     */
    fun parseArgument(
        property: KSPropertyDeclaration,
        packageName: String,
        knownClassType: KnownClassType
    ): ArgumentBinding? {
        val ksType = property.type.resolve()
        val paramType = ParamType.fromType(ksType)
        val accessor = FieldAccessor(property, enclosingClass)

        val error = getFieldError(property, knownClassType, paramType, accessor)
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

        // key 优先级：自定义 key > useFieldKey（字段名）> 默认（包名.字段名IntentKey）
        val defaultKey = "$packageName.${name}${FIELD_NAME_END}"
        val key: String = when {
            keyFromAnnotation.isNotEmpty() -> keyFromAnnotation
            useFieldKey -> name
            else -> defaultKey
        }

        val typeName: TypeName = ksType.toTypeName()

        // 收集非 @Boom 和非 @NotNull 的注解
        val annotationList = property.annotations
            .filter { it.shortName.asString() != Boom::class.simpleName && !it.shortName.asString().contains("NotNull") }
            .mapNotNull { it.annotationType.resolve().declaration.qualifiedName?.asString() }
            .toMutableList()

        // KSP 无法看到 Kotlin 编译器隐式的 @Nullable，需根据 nullability 手动补充
        val isPrimitiveType = paramType.isPrimitive()
        if (ksType.isMarkedNullable && !isPrimitiveType) {
            annotationList.add("org.jetbrains.annotations.Nullable")
        }

        return ArgumentBinding(name, key, paramType, typeName, index, isOptional, accessor, annotationList, desc)
    }

    private fun getFieldError(
        property: KSPropertyDeclaration,
        knownClassType: KnownClassType,
        paramType: ParamType?,
        accessor: FieldAccessor
    ): String? = when {
        enclosingClass.classKind != ClassKind.CLASS -> Errors.notAClass
        enclosingClass.modifiers.contains(Modifier.PRIVATE) -> Errors.privateClass
        paramType == null -> Errors.notSupportedType
        !accessor.isAccessible() -> Errors.inaccessibleField
        paramType.typeUsedBySupertype() && knownClassType == KnownClassType.BroadcastReceiver -> Errors.notBasicTypeInReceiver
        else -> null
    }
}
