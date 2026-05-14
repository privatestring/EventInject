package launcher.classbinding

import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import launcher.Boom
import launcher.MakeResult
import launcher.ParentCls
import launcher.Router
import launcher.error.Errors
import launcher.error.parsingError
import launcher.param.ArgumentFactory
import launcher.utils.getElementType
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

internal class ClassBindingFactory(val typeElement: TypeElement) {

    fun create(): ClassBinding? {
        try {
            val knownClassType: KnownClassType = KnownClassType.getByType(getElementType(typeElement))
            val error = getClassError(knownClassType)
            if (error != null) {
                parsingError<MakeResult>(error, typeElement, typeElement)
                return null
            }
            val targetTypeName = getTargetTypeName(typeElement)
            val bindingClassName = launcher.codegeneration.getBindingClassName(typeElement)
            val packageName = bindingClassName.packageName()
            val argumentFactory = ArgumentFactory(typeElement)
            val argumentBindings = typeElement.enclosedElements
                .filter { it.getAnnotation(Boom::class.java) != null }
                .mapNotNull { argumentFactory.parseArgument(it, packageName, knownClassType) }
                .sortedBy { it.index }
            val addStartForResult = typeElement.getAnnotation(MakeResult::class.java)?.includeStartForResult ?: false
            val isParentClass = typeElement.getAnnotation(ParentCls::class.java)?.isParentClass ?: false
            val routerPath = typeElement.getAnnotation(Router::class.java)?.routerPath ?: ""
            val cls = typeElement.getAnnotationClassValue<Router> { cls }
            return ClassBinding(
                knownClassType,
                targetTypeName,
                bindingClassName,
                packageName,
                argumentBindings,
                addStartForResult,
                routerPath,
                isParentClass,
                cls
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>) =
        try {
            getAnnotation(T::class.java)?.f()
            null
        } catch (e: MirroredTypeException) {
            e.typeMirror
        }

    private fun getClassError(elementType: KnownClassType?) = when {
        elementType == null -> Errors.wrongClassType
        else -> null
    }

    private fun getTargetTypeName(enclosingElement: TypeElement) = TypeName
            .get(enclosingElement.asType())
            .let { if (it is ParameterizedTypeName) it.rawType else it }
}