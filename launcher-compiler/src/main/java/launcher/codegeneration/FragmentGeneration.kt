package launcher.codegeneration

import com.squareup.javapoet.MethodSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.BUNDLE
import launcher.utils.doIf

internal class FragmentGeneration(classBinding: ClassBinding) : ClassGeneration(classBinding) {

    override fun createFillFieldsMethod() = getBasicFillMethodBuilder()
            .addParameter(classBinding.targetTypeName, "fragment")
            .doIf(classBinding.argumentBindings.isNotEmpty()) { addStatement("\$T arguments = fragment.getArguments()", BUNDLE) }
            .addBundleSetters("arguments", "fragment", true)
            .build()!!

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> = listOf(
            createGetFragmentMethod(variant)
    )

    private fun createGetFragmentMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext("newInstance")
            .addJavadoc("This is Method for new Fragment and put field\n")
            .addArgParameters(variant)
            .returns(classBinding.targetTypeName)
            .addGetFragmentCode(variant)
            .build()

    private fun MethodSpec.Builder.addGetFragmentCode(variant: List<ArgumentBinding>) = this
            .addStatement("\$T fragment = new \$T()", classBinding.targetTypeName, classBinding.targetTypeName)
            .doIf(variant.isNotEmpty()) {
                addStatement("\$T args = new Bundle()", BUNDLE)
                addSaveBundleStatements("args", variant, { it.name })
                addStatement("fragment.setArguments(args)")
            }
            .addStatement("return fragment")
}
