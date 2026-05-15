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

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * Launcher 代码生成基类。
 * 定义生成 XxxLauncher.java 的骨架：key 常量 + bind 方法 + 各种 starter 方法。
 * 子类按组件类型实现具体的 bind 和 starter 逻辑。
 */
internal abstract class ClassGeneration(val classBinding: ClassBinding) {

    /** 构建最终的 JavaFile，添加文件头注释 */
    fun brewJava(): JavaFile = JavaFile.builder(classBinding.packageName, createStarterSpec())
        .addFileComment("Generated code from ActivityLauncher. Do not modify!")
        .build()

    /** 子类实现：生成 bind() 方法 */
    abstract fun createFillFieldsMethod(): MethodSpec

    /** 子类实现：根据参数变体生成 starter 方法（startActivity/newInstance 等） */
    abstract fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec>

    /** 扩展点：子类可向生成类中追加额外方法（如 Router 的 putRouter） */
    open fun TypeSpec.Builder.addExtraToClass(): TypeSpec.Builder = this

    /** 扩展点：子类可在类顶部追加额外字段（如 Router 的 ROUTER_ACTION） */
    open fun TypeSpec.Builder.addExtraTop(): TypeSpec.Builder = this

    /** 创建 bind 方法的 Builder，带 Javadoc 和 public static 修饰 */
    protected fun getBasicFillMethodBuilder(fillProperCall: String = "ActivityLauncher.bind(this)"): MethodSpec.Builder = MethodSpec
        .methodBuilder(BIND_THIS_CLASS)
        .addJavadoc("This is method used to fill fields. Use it by calling $fillProperCall.\n")
        .addModifiers(PUBLIC, STATIC)

    /** 创建带 Context 参数的 public static 方法 Builder */
    protected fun builderWithCreationBasicFields(name: String): MethodSpec.Builder =
        builderWithCreationBasicFieldsNoContext(name)
            .addParameter(CONTEXT, "context")

    /** 创建不带 Context 的 public static 方法 Builder */
    protected fun builderWithCreationBasicFieldsNoContext(name: String): MethodSpec.Builder =
        MethodSpec.methodBuilder(name)
            .addModifiers(PUBLIC, STATIC)

    /** 为方法添加参数列表（含注解） */
    protected fun MethodSpec.Builder.addArgParameters(variant: List<ArgumentBinding>) = apply {
        variant.forEach { arg ->
            addParameter(
                ParameterSpec.builder(arg.typeName, arg.name).apply {
                    arg.annotationCls.forEach { addAnnotation(it) }
                }.build()
            )
        }
    }

    /** 生成 Bundle.putXxx 语句，引用类型加 null 检查 */
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

    /** 为所有字段生成 Bundle 读取赋值语句 */
    protected fun MethodSpec.Builder.addBundleSetters(bundleName: String, className: String, checkIfSet: Boolean) = apply {
        classBinding.argumentBindings.forEach { arg -> addBundleSetter(arg, bundleName, className, checkIfSet) }
    }

    /** 生成单个字段的 Bundle 读取赋值语句，基本类型检查 containsKey，引用类型额外检查非 null */
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

    /** 组装生成类的 TypeSpec：Javadoc + 修饰符 + 扩展字段 + key 常量 + 方法 */
    private fun createStarterSpec() = TypeSpec
        .classBuilder(classBinding.bindingClassName.simpleName())
        .addJavadoc("佛祖保佑         永无BUG\n如果代码正常就是Joker写的\n如果运行不通过我也不知道是谁写的\n")
        .addModifiers(PUBLIC, FINAL)
        .addExtraTop()
        .addKeyFields()
        .addClassMethods()
        .build()

    /** 添加所有 @Boom 字段对应的 KEY 常量（public static final String） */
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

    /** 添加 bind + 扩展方法 + 所有参数变体的 starter 方法 */
    private fun TypeSpec.Builder.addClassMethods() = this
        .addMethod(createFillFieldsMethod())
        .addExtraToClass()
        .addMethods(classBinding.argumentBindingVariants.flatMap { variant -> createStarters(variant) })
}
