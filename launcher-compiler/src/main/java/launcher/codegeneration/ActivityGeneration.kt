package launcher.codegeneration

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.ACTIVITY
import launcher.utils.START_RESULT_METHOD_NAME
import launcher.utils.addIf
import launcher.utils.doIf

internal class ActivityGeneration(classBinding: ClassBinding) : IntentBinding(classBinding) {

    override fun createFillFieldsMethod() = getBasicFillMethodBuilder()
        .addParameter(classBinding.targetTypeName, "activity")
        .addStatement("if(activity == null) return")
        .doIf(classBinding.argumentBindings.isNotEmpty()) { addFieldSettersCode() }
        .build()!!


    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> {
        if (classBinding.isParentClass) {
            return listOfNotNull(createIntentAddParamsMethod(variant))
        } else {
            return listOfNotNull(
                createGetIntentMethod(variant),
                createStartActivityMethod(variant)
            ).addIf(
                classBinding.includeStartForResult,
                createStartActivityForResultMethod(variant)
            )
        }
    }

    private fun MethodSpec.Builder.addFieldSettersCode() {
        addStatement("Intent intent = activity.getIntent()")
        for (arg in classBinding.argumentBindings) {
            addIntentSetter(arg, "activity")
        }
    }

    private fun createStartActivityMethod(variant: List<ArgumentBinding>) =
        createGetIntentStarter("startActivity", variant)


    private fun createStartActivityForResultMethod(variant: List<ArgumentBinding>) =
        builderWithCreationBasicFieldsNoContext(START_RESULT_METHOD_NAME)
            .addJavadoc("This is Method for StartActivity and get Result\n")
            .addParameter(ACTIVITY, "context")
            .addArgParameters(variant)
            .addParameter(TypeName.INT, "result")
            .addStatement("if(context == null) return")
            .addGetIntentStatement(variant)
            .addStatement("context.startActivityForResult(intent, result)")
            .build()

}