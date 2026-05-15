package launcher.codegeneration

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.BUNDLE
import launcher.utils.GET_INTENT_METHOD
import launcher.utils.INTENT

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * Model Launcher 代码生成。
 * 同时生成 Intent 和 Bundle 两种绑定方式：
 * bind(model, intent) / bind(model, bundle) / getIntentFrom(context, clazz, params) / getArguments(params)
 */
internal class ModelGeneration(classBinding: ClassBinding) : IntentBinding(classBinding) {

    override fun createFillFieldsMethod() = fillByIntentBinding("model")

    /** 通过 addExtraToClass 扩展点添加 bind(model, bundle) 方法，只生成一次 */
    override fun TypeSpec.Builder.addExtraToClass(): TypeSpec.Builder {
        addMethod(createBindFragment())
        return this
    }

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> {
        return arrayListOf<MethodSpec>().apply {
            add(createGetClassIntentMethod(variant))
            add(createGetArgumentsMethod(variant))
            if (classBinding.isParentClass) {
                add(createIntentAddParamsMethod(variant))
            }
        }
    }

    private fun createGetClassIntentMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFields(GET_INTENT_METHOD)
        .addParameter(ClassName.get(Class::class.java), "clazz")
        .addArgParameters(variant)
        .returns(INTENT)
        .addStatement("\$T intent = new Intent(context, clazz)", INTENT)
        .addPutExtraStatement(variant)
        .addStatement("return intent")
        .build()!!

    private fun createBindFragment() =
        getBasicFillMethodBuilder("${classBinding.bindingClassName.simpleName()}Launcher.bind(this, arguments)")
            .addParameter(classBinding.targetTypeName, "model")
            .addParameter(BUNDLE, "arguments")
            .addStatement("if(model == null || arguments == null) return")
            .addBundleSetters("arguments", "model", true)
            .build()!!

    private fun createGetArgumentsMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext("getArguments")
        .addArgParameters(variant)
        .returns(BUNDLE)
        .addStatement("\$T args = new Bundle()", BUNDLE)
        .addSaveBundleStatements("args", variant) { it.name }
        .addStatement("return args")
        .build()!!
}
