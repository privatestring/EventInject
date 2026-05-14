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
 * @author Created by joker on 2023/2/10
 */
internal class RouterGeneration(classBinding: ClassBinding) : ClassGeneration(classBinding) {

    override fun createFillFieldsMethod(): MethodSpec = getBasicFillMethodBuilder("ActivityLauncher.bind(this, intent)").build()

    override fun TypeSpec.Builder.addExtraToClass(): TypeSpec.Builder {
        createInitRouterMethod()?.let { addMethod(it) }
        return this
    }


    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> {
        return arrayListOf(
            createJumpMethod(variant),
            createJumpCallbackMethod(variant),
            createGetIntentMethod(variant),
        ).filterNotNull().toList()
    }

    private fun createInitRouterMethod() = if (classBinding.cls == null) null
    else {
        val clsPath = if (classBinding.cls.toString() == Void::class.java.name)
            "${classBinding.bindingClassName.packageName()}.${classBinding.bindingClassName.simpleName().replace("_XXXxxx", "")}"
        else "${classBinding.cls}"
        builderWithCreationBasicFieldsNoContext("putRouter")
            .addStatement("router.WBRouter.putRouter(ROUTER_ACTION, \"${clsPath}\")")
            .build()
    }

    private fun createJumpMethod(paramList:List<ArgumentBinding>) = if (classBinding.cls == null) null
    else {
        val builder = builderWithCreationBasicFields("jump").addDocument(paramList).addArgParameters(paramList)
        val jumpManagerClass = ClassName.get("com.webull.core.framework.jump", "JumpManager")
        val arguments = paramList.joinToString(", ") { it.name }
        if (paramList.isEmpty().not()){
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme(${arguments}))",jumpManagerClass)
                    .build()
        } else {
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme())",jumpManagerClass)
                    .build()
            }
        }

    private fun createJumpCallbackMethod(paramList:List<ArgumentBinding>) = if (classBinding.cls == null) null
    else {
        val callbackClassName = ClassName.get("launcher","IRouterJumpCallback")
        val builder = builderWithCreationBasicFields("jump").addDocument(paramList).addArgParameters(paramList).addParameter(callbackClassName,"callback")
        val jumpManagerClass = ClassName.get("com.webull.core.framework.jump", "JumpManager")
        val arguments = paramList.joinToString(", ") { it.name }
        if (paramList.isEmpty().not()){
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme(${arguments}), callback)",jumpManagerClass)
                .build()
        } else {
            builder.addStatement("\$T.jumpForRouter(context, getActionScheme(), callback)",jumpManagerClass)
                .build()
        }
    }

    private fun createGetIntentMethod(variant: List<ArgumentBinding>) = builderWithCreationBasicFieldsNoContext("getActionScheme")
        .addDocument(variant)
        .addArgParameters(variant)
        .returns(STRING).apply {
            if (variant.isEmpty()) addStatement("return ROUTER_ACTION")
            else {
                addStatement("\$T sb = new StringBuilder()", STRINGBUILDER)
                    .addStatement("sb.append(ROUTER_ACTION)")
                    .addPutExtraStatement(variant)
                    .addStatement("return sb.toString()")
            }
        }
        .build()!!

    override fun TypeSpec.Builder.addExtraTop(): TypeSpec.Builder {
        val fieldSpec = FieldSpec
            .builder(STRING, "ROUTER_ACTION", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
            .initializer("\"${classBinding.routerPath}\"")
            .build()
        addField(fieldSpec)
        return this
    }

    private fun MethodSpec.Builder.addDocument(variant: List<ArgumentBinding>) = apply {
        variant.forEach {
            if (it.desc.isEmpty()) {
                throw java.lang.IllegalArgumentException(
                    """
                    
                    ============================================================
                    
                    ${classBinding.targetTypeName}
                    ${it.name} 跨模块需要添加 desc 的描述
                    
                    
                    ============================================================
                    
                """.trimIndent()
                )
            }
        }
        addJavadoc(variant.joinToString("\n") { "@param ${it.name} ${it.desc}" })
    }

    private fun MethodSpec.Builder.addPutExtraStatement(variant: List<ArgumentBinding>) = apply {
        if (variant.isNotEmpty()) {
            addStatement("//后续考虑是否支持多种基础类型")
        }
        variant.forEach { arg ->
            // TODO: joker 2023/2/10 目前JumpManager只支持String，待完善
            val insert = {
                if (arg.typeName.checkNotBox().not()) beginControlFlow("if(${arg.name} != null)")
                addStatement("sb.append(sb.indexOf(\"?\") < 0 ? \"?\" : \"&\")")
                addStatement("//${arg.paramType.name}")
                addStatement("sb.append(launcher.JokeUtils.addUrlParam(${arg.fieldName},${arg.name}))")
                if (arg.typeName.checkNotBox().not()) endControlFlow()
            }
            val supportType = arrayListOf(String::class.java.name)
            val typeNameStr = arg.typeName.toString()
            if (supportType.contains(typeNameStr)) {
                insert()
            } else {
                throw java.lang.IllegalArgumentException(
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