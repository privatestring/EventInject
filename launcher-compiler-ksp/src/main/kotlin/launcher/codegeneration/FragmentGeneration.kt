package launcher.codegeneration

import com.squareup.javapoet.MethodSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.BUNDLE
import launcher.utils.addIf
import launcher.utils.doIf

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * Fragment Launcher 代码生成。
 * 生成：bind(fragment) / newInstance(params) / getBundleFrom(params)
 * ParentCls 模式下额外生成 addBundleParams(args, params)
 */
internal class FragmentGeneration(classBinding: ClassBinding) : ClassGeneration(classBinding) {

    override fun createFillFieldsMethod() = getBasicFillMethodBuilder()
        .addParameter(classBinding.targetTypeName, "fragment")
        .doIf(classBinding.argumentBindings.isNotEmpty()) {
            addStatement("\$T arguments = fragment.getArguments()", BUNDLE)
            addStatement("if(arguments == null) return")
        }
        .addBundleSetters("arguments", "fragment", true)
        .build()

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> = listOf(
        createGetFragmentMethod(variant),
        getArguments(variant)
    ).addIf(classBinding.isParentClass, addParamsMethod(variant))

    private fun getArguments(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext("getBundleFrom")
        .addParamJavadoc(variant)
        .addArgParameters(variant)
        .returns(BUNDLE)
        .addStatement("\$T args = new Bundle()", BUNDLE)
        .doIf(variant.isNotEmpty()) {
            doIf(classBinding.isParentClass) {
                val arguments = variant.joinToString(separator = ", ", transform = { it.name })
                addStatement("addBundleParams(args, $arguments)")
            }
            doIf(!classBinding.isParentClass) {
                addSaveBundleStatements("args", variant) { it.name }
            }
        }
        .addStatement("return args")
        .build()

    private fun createGetFragmentMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext("newInstance")
        .addParamJavadoc(variant)
        .addArgParameters(variant)
        .returns(classBinding.targetTypeName)
        .addGetFragmentCode(variant)
        .build()

    private fun MethodSpec.Builder.addGetFragmentCode(variant: List<ArgumentBinding>) = this
        .addStatement("\$T fragment = new \$T()", classBinding.targetTypeName, classBinding.targetTypeName)
        .doIf(variant.isNotEmpty()) {
            val arguments = variant.joinToString(separator = ", ", transform = { it.name })
            addStatement("\$T args = getBundleFrom($arguments)", BUNDLE)
            addStatement("fragment.setArguments(args)")
        }
        .addStatement("return fragment")

    private fun addParamsMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext("addBundleParams")
        .addJavadoc("This is Method for child add Parent of params, it need exits Bundle\n")
        .addParameter(BUNDLE, "args")
        .addArgParameters(variant)
        .beginControlFlow("if(args != null)")
        .addSaveBundleStatements("args", variant) { it.name }
        .endControlFlow()
        .build()
}
