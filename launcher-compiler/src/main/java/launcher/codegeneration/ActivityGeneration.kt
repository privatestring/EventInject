package launcher.codegeneration

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.*

internal class ActivityGeneration(classBinding: ClassBinding) : IntentBinding(classBinding) {

    override fun createFillFieldsMethod() = getBasicFillMethodBuilder()
            .addParameter(classBinding.targetTypeName, "activity")
//            .addParameter(BUNDLE, "savedInstanceState")
            .doIf(classBinding.argumentBindings.isNotEmpty()) { addFieldSettersCode() }
            .build()!!

    override fun TypeSpec.Builder.addExtraToClass() = this
//            .addMethod(createSaveMethod())

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> {
        if (classBinding.isParentClass) {
           return listOfNotNull(createIntentAddParamsMethod(variant))
        } else {
           return listOfNotNull(
                    createGetIntentMethod(variant),
                    createStartActivityMethod(variant)
//            ,createStartActivityMethodWithFlags(variant)
            ).addIf(classBinding.includeStartForResult,
                    createStartActivityForResultMethod(variant)
//            ,createStartActivityForResultMethodWithFlags(variant)
            )
        }
    }

    private fun MethodSpec.Builder.addFieldSettersCode() {
        addStatement("Intent intent = activity.getIntent()")
        for (arg in classBinding.argumentBindings) {
//            val bundleName = "savedInstanceState"
//            val bundlePredicate = getBundlePredicate(bundleName, arg.fieldName)
//            addCode("if($bundleName != null && $bundlePredicate) {\n")
//            addBundleSetter(arg, bundleName, "activity", false)
//            addCode("} else ")
            addIntentSetter(arg, "activity")
        }
    }

    private fun createStartActivityMethod(variant: List<ArgumentBinding>) =
            createGetIntentStarter("startActivity", variant)


    private fun createStartActivityForResultMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext(START_RESULT_METHOD_NAME)
            .addJavadoc("This is Method for StartActivity and get Result\n")
            .addParameter(ACTIVITY, "context")
            .addArgParameters(variant)
            .addParameter(TypeName.INT, "result")
            .addGetIntentStatement(variant)
            .addStatement("context.startActivityForResult(intent, result)")
            .build()

    private fun createSaveMethod(): MethodSpec = this
            .builderWithCreationBasicFieldsNoContext(SAVE_BUNDLE)
            .addParameter(classBinding.targetTypeName, "activity")
            .addParameter(BUNDLE, "bundle")
            .addSaveBundleStatements("bundle", classBinding.argumentBindings, { "activity.${it.accessor.getFieldValue()}" })
            .build()

    private fun createStartActivityMethodWithFlags(variant: List<ArgumentBinding>) = builderWithCreationBasicFields("startWithFlags")
            .addArgParameters(variant)
            .addParameter(TypeName.INT, "flags")
            .addGetIntentStatement(variant)
            .addStatement("intent.addFlags(flags)")
            .addStatement("context.startActivity(intent)")
            .build()

    private fun createStartActivityForResultMethodWithFlags(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext("startWithFlagsForResult")
            .addParameter(ACTIVITY, "context")
            .addArgParameters(variant)
            .addParameter(TypeName.INT, "result")
            .addParameter(TypeName.INT, "flags")
            .addGetIntentStatement(variant)
            .addStatement("intent.addFlags(flags)")
            .addStatement("context.startActivityForResult(intent, result)")
            .build()
}