package launcher.codegeneration

import com.squareup.javapoet.MethodSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.ADD_INTENT_PARAMS
import launcher.utils.GET_INTENT_METHOD
import launcher.utils.INTENT
import launcher.utils.START_METHOD_NAME
import launcher.utils.checkNotBox

/**
 * Intent 绑定基类，Activity/Model/BroadcastReceiver 共用
 */
internal abstract class IntentBinding(classBinding: ClassBinding) : ClassGeneration(classBinding) {

    protected fun fillByIntentBinding(targetName: String) = getBasicFillMethodBuilder("ActivityLauncher.bind(this, intent)")
        .addParameter(classBinding.targetTypeName, targetName)
        .addParameter(INTENT, "intent")
        .addStatement("if(intent == null || $targetName == null) return")
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
        .addJavadoc("This is Method for childActivity add ParentActivity of params, it need exits Intent\n")
        .addParameter(INTENT, "intent")
        .addArgParameters(variant)
        .returns(INTENT)
        .beginControlFlow("if(intent != null)")
        .addPutExtraStatement(variant)
        .endControlFlow()
        .addStatement("return intent")
        .build()!!

    protected fun MethodSpec.Builder.addPutExtraStatement(variant: List<ArgumentBinding>) = apply {
        variant.forEach { arg ->
            val putMethodName = getPutArgumentToIntentMethodName(arg.paramType)
            // 基本类型不需要 null 检查
            val needsNullCheck = !arg.typeName.checkNotBox() && !arg.paramType.isPrimitive()
            if (needsNullCheck) beginControlFlow("if(${arg.name} != null)")
            addStatement("intent.$putMethodName(" + arg.fieldName + ", " + arg.name + ");//${arg.paramType.name}")
            if (needsNullCheck) endControlFlow()
        }
    }

    protected fun MethodSpec.Builder.addIntentSetters(targetParameterName: String) = apply {
        classBinding.argumentBindings.forEach { arg -> addIntentSetter(arg, targetParameterName) }
    }

    protected fun MethodSpec.Builder.addIntentSetter(arg: ArgumentBinding, targetParameterName: String) {
        val fieldName = arg.fieldName
        val what = getIntentGetterFor(arg.paramType, arg.typeName, fieldName)
        val settingPart = arg.accessor.setToField(what)
        // 基本类型（包括 boxed 的 nullable 基本类型）只需检查 hasExtra
        if (arg.typeName.checkNotBox() || arg.paramType.isPrimitive()) {
            beginControlFlow("if(intent.hasExtra($fieldName))")
        } else {
            val getter = getIntentGetterForParamType(arg.paramType, fieldName)
            beginControlFlow("if(intent.hasExtra($fieldName) && intent.${getter} != null)")
        }
        addStatement("$targetParameterName.$settingPart")
        endControlFlow()
    }

    protected fun createGetIntentStarter(starterFunc: String, variant: List<ArgumentBinding>) =
        builderWithCreationBasicFields(START_METHOD_NAME)
            .addArgParameters(variant)
            .addStatement("if(context == null) return")
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
