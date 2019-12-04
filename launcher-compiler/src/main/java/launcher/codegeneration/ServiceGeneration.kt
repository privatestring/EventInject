package launcher.codegeneration

import launcher.param.ArgumentBinding
import com.squareup.javapoet.MethodSpec
import launcher.classbinding.ClassBinding

internal class ServiceGeneration(classBinding: ClassBinding) : IntentBinding(classBinding) {

    override fun createFillFieldsMethod(): MethodSpec = fillByIntentBinding("service")

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> = listOf(
            createGetIntentMethod(variant),
            createStartServiceMethod(variant)
    )

    private fun createStartServiceMethod(variant: List<ArgumentBinding>) =
            createGetIntentStarter("startService", variant)
}