package launcher.classbinding

import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import launcher.Boom
import launcher.MakeResult
import launcher.ParentCls
import launcher.error.Errors
import launcher.error.parsingError
import launcher.param.ArgumentFactory
import launcher.utils.getElementType
import javax.lang.model.element.TypeElement

internal class ClassBindingFactory(val typeElement: TypeElement) {

    fun create(): ClassBinding? {
        try {
            val knownClassType: KnownClassType? = KnownClassType.getByType(getElementType(typeElement))
            val error = getClassError(knownClassType)
            if (error != null) {
                parsingError<MakeResult>(error, typeElement, typeElement)
                return null
            }
            knownClassType!!
            val targetTypeName = getTargetTypeName(typeElement)
            val bindingClassName = launcher.codegeneration.getBindingClassName(typeElement)
            val packageName = bindingClassName.packageName()
            val argumentFactory = ArgumentFactory(typeElement)
            val argumentBindings = typeElement.enclosedElements
                    .filter { it.getAnnotation(Boom::class.java) != null }
                    .map { argumentFactory.parseArgument(it, packageName, knownClassType) }
                    .filterNotNull()
            val addStartForResult = typeElement.getAnnotation(MakeResult::class.java)?.includeStartForResult ?: false
            val isParentClass = typeElement.getAnnotation(ParentCls::class.java)?.isParentClass ?: false

            return ClassBinding(knownClassType, targetTypeName, bindingClassName, packageName, argumentBindings, addStartForResult,isParentClass)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getClassError(elementType: KnownClassType?) = when {
        elementType == null -> Errors.wrongClassType
        else -> null
    }

    private fun getTargetTypeName(enclosingElement: TypeElement) = TypeName
            .get(enclosingElement.asType())
            .let { if (it is ParameterizedTypeName) it.rawType else it }
}