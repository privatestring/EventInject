package launcher.codegeneration

import com.squareup.javapoet.MethodSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.*

internal abstract class IntentBinding(classBinding: ClassBinding) : ClassGeneration(classBinding) {

    protected fun fillByIntentBinding(targetName: String) = getBasicFillMethodBuilder("ActivityLauncher.bind(this, intent)")
            .addParameter(classBinding.targetTypeName, targetName)
            .addParameter(INTENT, "intent")
            .addIntentSetters(targetName)
            .build()!!

    protected fun createGetIntentMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFields(GET_INTENT_METHOD)
            .addArgParameters(variant)
            .returns(INTENT)
            .addStatement("\$T intent = new Intent(context, \$T.class)", INTENT, classBinding.targetTypeName)
            .addPutExtraStatement(variant)
            .addStatement("return intent")
            .build()!!

    protected fun createIntentAddParamsMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext(ADD_INTENT_PARAMS)
            .addJavadoc("This is Method for childActivity add ParentActivity of params , it need exits Intent\n")
            .addParameter(INTENT, "intent")
            .addArgParameters(variant)
            .returns(INTENT)
            .beginControlFlow("if(intent != null)")
            .addPutExtraStatement(variant)
            .endControlFlow()
            .addStatement("return intent")
            .build()!!

    private fun MethodSpec.Builder.addPutExtraStatement(variant: List<ArgumentBinding>) = apply {
        variant.forEach { arg ->
            val putArgumentToIntentMethodName = getPutArgumentToIntentMethodName(arg.paramType)
            val check = if (arg.typeName.checkNotBox()) " " else "if(${arg.name} != null) \n"
//            val check = ""
//            if (arg.typeName.checkNotBox()) {
            addStatement(check + "intent.$putArgumentToIntentMethodName(" + arg.fieldName + ", " + arg.name + ")")
//            } else {
////                addStatement("if(${arg.name} != null) \n intent.$putArgumentToIntentMethodName(" + arg.fieldName + ", " + arg.name + ")")
//            }
        }
    }


    protected fun MethodSpec.Builder.addIntentSetters(targetParameterName: String) = apply {
        classBinding.argumentBindings.forEach { arg -> addIntentSetter(arg, targetParameterName) }
    }

    protected fun MethodSpec.Builder.addIntentSetter(arg: ArgumentBinding, targetParameterName: String) {
        val fieldName = arg.fieldName
        val settingPart = arg.accessor.setToField(getIntentGetterFor(arg.paramType, arg.typeName, fieldName))
        addStatement("if(intent.hasExtra($fieldName)) \n $targetParameterName.$settingPart")
    }

    protected fun createGetIntentStarter(starterFunc: String, variant: List<ArgumentBinding>) = builderWithCreationBasicFields(START_METHOD_NAME)
            .addArgParameters(variant)
            .addGetIntentStatement(variant)
            .addStatement("context.$starterFunc(intent)")
            .build()

    protected fun MethodSpec.Builder.addGetIntentStatement(variant: List<ArgumentBinding>) = apply {
        if (variant.isEmpty())
            addStatement("\$T intent = ${GET_INTENT_METHOD}(context)", INTENT)
        else {
            val intentArguments = variant.joinToString(separator = ", ", transform = { it.name })
            addStatement("\$T intent = ${GET_INTENT_METHOD}(context, $intentArguments)", INTENT)
        }
    }
}

