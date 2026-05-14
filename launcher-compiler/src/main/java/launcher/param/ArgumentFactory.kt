package launcher.param

import com.squareup.javapoet.TypeName
import launcher.Boom
import launcher.classbinding.KnownClassType
import launcher.error.Errors
import launcher.error.error
import launcher.utils.FIELD_NAME_END
import launcher.utils.getElementType
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * 属性生成器
 */
class ArgumentFactory(val enclosingElement: TypeElement) {

    fun parseArgument(element: Element, packageName: String, knownClassType: KnownClassType): ArgumentBinding? {
        val elementType: TypeMirror = getElementType(element)
        val paramType: ParamType? = ParamType.fromType(elementType)
        val error = getFieldError(element, knownClassType, paramType)
        if (error != null) {
            showProcessingError(element, error)
            return null
        }
        paramType!!
        val name: String = element.simpleName.toString()
        val boomAnnotation = element.getAnnotation(Boom::class.java)
        val keyFromAnnotation = boomAnnotation?.key
        val defaultKey = "$packageName.${name}${FIELD_NAME_END}"
        val key: String = if (keyFromAnnotation.orEmpty().isNotEmpty()) keyFromAnnotation.orEmpty()
        else if (boomAnnotation?.useFieldKey == true) name
        else defaultKey
        val typeName: TypeName = TypeName.get(elementType)
        val isOptional: Boolean = boomAnnotation?.isOptional ?: false
        val index = boomAnnotation?.index ?: 0
        val accessor = FieldAccessor(element)
        val annotationList = element.annotationMirrors.orEmpty().mapNotNull {
            kotlin.runCatching {
                it.annotationType.toString()
            }.getOrNull()
        }.filter { it != Boom::class.java.name  && it.contains("NotNull").not()}
        val desc = boomAnnotation?.desc ?: ""
        return ArgumentBinding(name, key, paramType, typeName, index, isOptional, accessor, annotationList,desc)
    }

    private fun getFieldError(element: Element, knownClassType: KnownClassType, paramTypeNullable: ParamType?) = when {
        enclosingElement.kind != ElementKind.CLASS -> Errors.notAClass
        enclosingElement.modifiers.contains(Modifier.PRIVATE) -> Errors.privateClass
        paramTypeNullable == null -> Errors.notSupportedType
        !FieldAccessor(element).isAccessible() -> Errors.inaccessibleField
        paramTypeNullable.typeUsedBySupertype() && knownClassType == KnownClassType.BroadcastReceiver -> Errors.notBasicTypeInReceiver
        else -> null
    }

    fun showProcessingError(element: Element, text: String) {
        error(enclosingElement, "@%s %s $text (%s)", "Arg", enclosingElement.qualifiedName, element.simpleName)
    }
}