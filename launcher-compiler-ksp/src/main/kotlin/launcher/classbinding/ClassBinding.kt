package launcher.classbinding

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import launcher.codegeneration.ActivityGeneration
import launcher.codegeneration.BroadcastReceiverGeneration
import launcher.codegeneration.ClassGeneration
import launcher.codegeneration.FragmentGeneration
import launcher.codegeneration.ModelGeneration
import launcher.param.ArgumentBinding
import launcher.utils.createSublists

internal class ClassBinding(
    val knownClassType: KnownClassType,
    val targetTypeName: TypeName,
    var bindingClassName: ClassName,
    val packageName: String,
    val argumentBindings: List<ArgumentBinding>,
    val includeStartForResult: Boolean,
    val isParentClass: Boolean
) {
    val argumentBindingVariants: List<List<ArgumentBinding>>
        get() = argumentBindings
            .createSublists { it.isOptional }
            .distinctBy { it.map { binding -> binding.typeName } }

    internal fun getClassGeneration(): ClassGeneration = when (knownClassType) {
        KnownClassType.Activity -> ActivityGeneration(this)
        KnownClassType.Fragment -> FragmentGeneration(this)
        KnownClassType.BroadcastReceiver -> BroadcastReceiverGeneration(this)
        KnownClassType.Model -> ModelGeneration(this)
    }
}
