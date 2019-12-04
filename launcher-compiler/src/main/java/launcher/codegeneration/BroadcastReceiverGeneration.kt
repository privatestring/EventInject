package launcher.codegeneration

import launcher.codegeneration.IntentBinding
import launcher.param.ArgumentBinding
import com.squareup.javapoet.MethodSpec
import launcher.classbinding.ClassBinding

internal class BroadcastReceiverGeneration(classBinding: ClassBinding) : IntentBinding(classBinding) {

    override fun createFillFieldsMethod() = fillByIntentBinding("receiver")

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> = listOf(
            createGetIntentMethod(variant)
    )
}