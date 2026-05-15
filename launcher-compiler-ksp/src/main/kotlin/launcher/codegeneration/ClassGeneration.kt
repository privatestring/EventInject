package launcher.codegeneration

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.param.ParamType
import launcher.utils.BIND_THIS_CLASS
import launcher.utils.CLASS_NAME_END
import launcher.utils.CONTEXT
import launcher.utils.STRING
import launcher.utils.checkNotBox
import launcher.utils.doIf
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

internal abstract class ClassGeneration(val classBinding: ClassBinding) {

    fun brewJava(): JavaFile = JavaFile.builder(classBinding.packageName, createStarterSpec())
        .addFileComment("Generated code from ActivityLauncher. Do not modify!")
        .build()

    abstract fun createFillFieldsMethod(): MethodSpec

    abstract fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec>

    protected fun getBasicFillMethodBuilder(fillProperCall: String = "ActivityLauncher.bind(this)"): MethodSpec.Builder = MethodSpec
        .methodBuilder(BIND_THIS_CLASS)
        .addJavadoc("This is method used to fill fields. Use it by calling $fillProperCall.\n")
        .addModifiers(PUBLIC, STATIC)

    protected fun builderWithCreationBasicFields(name: String): MethodSpec.Builder =
        builderWithCreationBasicFieldsNoContext(name)
            .addParameter(CONTEXT, "context")

    protected fun builderWithCreationBasicFieldsNoContext(name: String): MethodSpec.Builder =
        MethodSpec.methodBuilder(name)
            .addModifiers(PUBLIC, STATIC)

    protected fun MethodSpec.Builder.addArgParameters(variant: List<ArgumentBinding>) = apply {
        variant.forEach { arg ->
            addParameter(
                ParameterSpec.builder(arg.typeName, arg.name).apply {
                    arg.annotationCls.forEach { addAnnotation(it) }
                }.build()
            )
        }
    }

    protected fun MethodSpec.Builder.addSaveBundleStatements(
        bundleName: String,
        variant: List<ArgumentBinding>,
        argumentGetByName: (ArgumentBinding) -> String
    ) = apply {
        variant.forEach { arg ->
            val nullEnableType = arrayOf(ParamType.String).contains(arg.paramType)
            // 基本类型不需要 null 检查
            val hasControl = (!arg.typeName.checkNotBox() && !arg.paramType.isPrimitive()) || nullEnableType
            doIf(hasControl) { beginControlFlow("if(${arg.name} != null)") }
            val bundleSetter = getBundleSetterFor(arg.paramType)
            addStatement("$bundleName.$bundleSetter(" + arg.fieldName + ", " + argumentGetByName(arg) + ")")
            doIf(hasControl) { endControlFlow() }
        }
    }

    protected fun MethodSpec.Builder.addBundleSetters(bundleName: String, className: String, checkIfSet: Boolean) = apply {
        classBinding.argumentBindings.forEach { arg -> addBundleSetter(arg, bundleName, className, checkIfSet) }
    }

    protected fun MethodSpec.Builder.addBundleSetter(arg: ArgumentBinding, bundleName: String, className: String, checkIfSet: Boolean) {
        val fieldName = arg.fieldName
        val bundleGetter = getBundleGetter(bundleName, arg.paramType, arg.typeName, fieldName)
        val settingPart = arg.accessor.setToField(bundleGetter)
        if (checkIfSet) {
            // 基本类型（包括 boxed 的 nullable 基本类型）只需检查 containsKey
            if (arg.typeName.checkNotBox() || arg.paramType.isPrimitive()) {
                beginControlFlow("if(${getBundlePredicate(bundleName, fieldName)})")
            } else {
                val getter = getBundleGetterCall(arg.paramType)
                beginControlFlow("if(${getBundlePredicate(bundleName, fieldName)} && $bundleName.$getter(${fieldName})!=null )")
            }
        }
        addStatement("$className.$settingPart")
        if (checkIfSet) {
            endControlFlow()
        }
    }

    protected fun getBundlePredicate(bundleName: String, key: String) = "$bundleName.containsKey($key)"

    private fun createStarterSpec() = TypeSpec
        .classBuilder(classBinding.bindingClassName.simpleName())
        .addJavadoc("佛祖保佑         永无BUG\n如果代码正常就是Joker写的\n如果运行不通过我也不知道是谁写的\n")
        .addModifiers(PUBLIC, FINAL)
        .addKeyFields()
        .addClassMethods()
        .build()

    private fun TypeSpec.Builder.addKeyFields(): TypeSpec.Builder {
        for (arg in classBinding.argumentBindings) {
            val fieldSpec = FieldSpec
                .builder(STRING, arg.fieldName, STATIC, FINAL, PUBLIC)
                .initializer("\$S", arg.key)
                .build()
            addField(fieldSpec)
        }
        return this
    }

    private fun TypeSpec.Builder.addClassMethods() = this
        .addMethod(createFillFieldsMethod())
        .addMethods(classBinding.argumentBindingVariants.flatMap { variant -> createStarters(variant) })
}
