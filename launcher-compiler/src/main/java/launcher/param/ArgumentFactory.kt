package launcher.param

import com.squareup.javapoet.TypeName
import com.sun.org.apache.xpath.internal.Arg
import launcher.Boom
import launcher.MulField
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
        val keyFromAnnotation = element.getAnnotation(Boom::class.java)?.key
        val defaultKey = "$packageName.${name}${FIELD_NAME_END}"
        val key: String = if (keyFromAnnotation.isNullOrBlank()) defaultKey else keyFromAnnotation
        val typeName: TypeName = TypeName.get(elementType)
        val isOptional: Boolean = element.getAnnotation(MulField::class.java) != null
        val accessor: FieldAccessor = FieldAccessor(element)
        return ArgumentBinding(name, key, paramType, typeName, isOptional, accessor)
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
        error(enclosingElement, "@%s %s $text (%s)", Arg::class.java.simpleName, enclosingElement.qualifiedName, element.simpleName)
    }
}