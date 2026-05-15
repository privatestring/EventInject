package launcher.codegeneration

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding
import launcher.utils.STRING
import launcher.utils.STRINGBUILDER
import launcher.utils.checkNotBox
import javax.lang.model.element.Modifier

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * Router 路由代码生成器。
 * 为标注了 @Router 的类生成 {ClassName}_XXXxxx.java，包含：
 * - ROUTER_ACTION 常量（路由路径）
 * - putRouter() 注册路由到全局路由表
 * - jump(context, params...) 通过 JumpManager 跳转
 * - jump(context, params..., callback) 带回调跳转
 * - getActionScheme(params...) 拼接完整 URL scheme
 *
 * 限制：目前仅支持 String 类型参数，非 String 会编译报错。
 */
internal class RouterGeneration(classBinding: ClassBinding) : ClassGeneration(classBinding) {

    /** 空实现，Router 类不需要 bind 方法体 */
    override fun createFillFieldsMethod(): MethodSpec =
        getBasicFillMethodBuilder("ActivityLauncher.bind(this, intent)").build()

    /** 添加 putRouter() 到生成类 */
    override fun TypeSpec.Builder.addExtraToClass(): TypeSpec.Builder {
        createInitRouterMethod()?.let { addMethod(it) }
        return this
    }

    /** 每个参数变体生成 jump / jump+callback / getActionScheme 三个方法 */
    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> {
        return listOfNotNull(
            createJumpMethod(variant),
            createJumpCallbackMethod(variant),
            createGetActionSchemeMethod(variant),
        )
    }

    /** 生成 putRouter()：注册路由路径到全局路由表 WBRouter */
    private fun createInitRouterMethod(): MethodSpec? {
        if (classBinding.routerPath.isEmpty()) return null
        val clsPath = if (classBinding.cls == null) {
            "${classBinding.bindingClassName.packageName()}.${classBinding.bindingClassName.simpleName().replace("_XXXxxx", "")}"
        } else {
            classBinding.cls
        }
        return builderWithCreationBasicFieldsNoContext("putRouter")
            .addStatement("router.WBRouter.putRouter(ROUTER_ACTION, \"${clsPath}\")")
            .build()
    }

    /** 生成 jump(context, params...)：通过 JumpManager 执行路由跳转 */
    private fun createJumpMethod(paramList: List<ArgumentBinding>): MethodSpec? {
        if (classBinding.routerPath.isEmpty()) return null
        val builder = builderWithCreationBasicFields("jump")
            .addDocument(paramList)
            .addArgParameters(paramList)
        val jumpManagerClass = ClassName.get("com.webull.core.framework.jump", "JumpManager")
        val arguments = paramList.joinToString(", ") { it.name }
        return if (paramList.isNotEmpty()) {
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme(${arguments}))", jumpManagerClass)
                .build()
        } else {
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme())", jumpManagerClass)
                .build()
        }
    }

    /** 生成 jump(context, params..., callback)：带自定义跳转回调 */
    private fun createJumpCallbackMethod(paramList: List<ArgumentBinding>): MethodSpec? {
        if (classBinding.routerPath.isEmpty()) return null
        val callbackClassName = ClassName.get("launcher", "IRouterJumpCallback")
        val builder = builderWithCreationBasicFields("jump")
            .addDocument(paramList)
            .addArgParameters(paramList)
            .addParameter(callbackClassName, "callback")
        val jumpManagerClass = ClassName.get("com.webull.core.framework.jump", "JumpManager")
        val arguments = paramList.joinToString(", ") { it.name }
        return if (paramList.isNotEmpty()) {
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme(${arguments}), callback)", jumpManagerClass)
                .build()
        } else {
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme(), callback)", jumpManagerClass)
                .build()
        }
    }

    /** 生成 getActionScheme(params...)：拼接 "routerPath?key=value&..." 格式的 URL */
    private fun createGetActionSchemeMethod(variant: List<ArgumentBinding>): MethodSpec {
        return builderWithCreationBasicFieldsNoContext("getActionScheme")
            .addDocument(variant)
            .addArgParameters(variant)
            .returns(STRING).apply {
                if (variant.isEmpty()) {
                    addStatement("return ROUTER_ACTION")
                } else {
                    addStatement("\$T sb = new StringBuilder()", STRINGBUILDER)
                        .addStatement("sb.append(ROUTER_ACTION)")
                        .addPutExtraStatement(variant)
                        .addStatement("return sb.toString()")
                }
            }
            .build()
    }

    /** 在类顶部添加 ROUTER_ACTION 常量字段 */
    override fun TypeSpec.Builder.addExtraTop(): TypeSpec.Builder {
        val fieldSpec = FieldSpec
            .builder(STRING, "ROUTER_ACTION", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
            .initializer("\$S", classBinding.routerPath)
            .build()
        addField(fieldSpec)
        return this
    }

    /** 校验 @Boom.desc 必填，并生成 @param Javadoc */
    private fun MethodSpec.Builder.addDocument(variant: List<ArgumentBinding>) = apply {
        variant.forEach {
            if (it.desc.isEmpty()) {
                throw IllegalArgumentException(
                    """
                    
                    ============================================================
                    
                    ${classBinding.targetTypeName}
                    ${it.name} 跨模块需要添加 desc 的描述
                    
                    
                    ============================================================
                    
                """.trimIndent()
                )
            }
        }
        if (variant.isNotEmpty()) {
            addJavadoc(variant.joinToString("\n") { "@param ${it.name} ${it.desc}" })
        }
    }

    /** 拼接 URL 参数，仅支持 String 类型，非 String 编译报错 */
    private fun MethodSpec.Builder.addPutExtraStatement(variant: List<ArgumentBinding>) = apply {
        if (variant.isNotEmpty()) {
            addStatement("//后续考虑是否支持多种基础类型")
        }
        variant.forEach { arg ->
            val typeNameStr = arg.typeName.toString()
            val supportType = arrayListOf(String::class.java.name)
            if (supportType.contains(typeNameStr)) {
                if (arg.typeName.checkNotBox().not()) beginControlFlow("if(${arg.name} != null)")
                addStatement("sb.append(sb.indexOf(\"?\") < 0 ? \"?\" : \"&\")")
                addStatement("//${arg.paramType.name}")
                addStatement("sb.append(launcher.JokeUtils.addUrlParam(${arg.fieldName},${arg.name}))")
                if (arg.typeName.checkNotBox().not()) endControlFlow()
            } else {
                throw IllegalArgumentException(
                    """
                    
                    ============================================================
                    
                    ${arg.typeName}
                    ${classBinding.targetTypeName}
                    ${arg.name} 目前JumpManager只支持String，待完善  ， 跨模块间只能用基础数据结构传递
                    
                    
                    ============================================================
                    
                """.trimIndent()
                )
            }
        }
    }
}
