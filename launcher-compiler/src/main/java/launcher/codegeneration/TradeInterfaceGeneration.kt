package launcher.codegeneration

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

/**
 * TradeInterface注解处理器的代码生成类
 * 用于生成TradeInterfaceFactory代码
 */
class TradeInterfaceGeneration(
    private val regularInterfaces: Map<String, String>,
    private val innerInterfaces: Map<String, String>,
    private val moduleName: String
) {

    fun brewJava(): JavaFile = JavaFile.builder("com.webull.trade.services", createFactoryClass())
        .addFileComment("Generated code from TradeInterface annotation processor!")
        .build()

    private fun createFactoryClass(): TypeSpec {
        return TypeSpec.classBuilder("TradeInterfaceFactory$moduleName")
            .addJavadoc("自动生成的TradeInterfaceFactory类\n由TradeInterface注解处理器生成\n")
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get("com.webull.commonmodule.trade.service.trade.base", "ITradeInterfaceFactory"))
            .addMethod(createCreateInstanceMethod())
            .addMethod(createInnerInstanceMethod())
            .build()
    }

    private fun createCreateInstanceMethod(): MethodSpec {
        val iTradeInterface = ClassName.get("com.webull.commonmodule.trade.service.trade.base", "ITradeInterface")
        val classType = ParameterizedTypeName.get(ClassName.get(Class::class.java), WildcardTypeName.subtypeOf(TypeVariableName.get("T")))
        
        val methodBuilder = MethodSpec.methodBuilder("createInstance")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(TypeVariableName.get("T", iTradeInterface))
            .returns(iTradeInterface)
            .addParameter(
                ParameterSpec.builder(classType, "clazz")
                .addModifiers(Modifier.FINAL)
                .build()
            )
            .addStatement("String className = clazz.getName()")
            .addCode("\n")
            .beginControlFlow("switch (className)")
            
        // 添加普通接口的case
        regularInterfaces.forEach { (interfaceClass, implClass) ->
            methodBuilder.addCode(
                "case \$S:\n" +
                "    return new \$L();\n", 
                interfaceClass, implClass
            )
        }
        
        // 处理默认情况，调用内部接口处理方法
        methodBuilder.addCode(
            "default:\n" +
            "    return createInnerInstance(clazz);\n"
        )
        
        methodBuilder.endControlFlow()
        
        return methodBuilder.build()
    }

    private fun createInnerInstanceMethod(): MethodSpec {
        val iTradeInterface = ClassName.get("com.webull.commonmodule.trade.service.trade.base", "ITradeInterface")
        val classType = ParameterizedTypeName.get(ClassName.get(Class::class.java), WildcardTypeName.subtypeOf(TypeVariableName.get("T")))
        
        val methodBuilder = MethodSpec.methodBuilder("createInnerInstance")
            .addModifiers(Modifier.PRIVATE)
            .addTypeVariable(TypeVariableName.get("T", iTradeInterface))
            .returns(iTradeInterface)
            .addParameter(
                ParameterSpec.builder(classType, "clazz")
                .addModifiers(Modifier.FINAL)
                .build()
            )
            .addStatement("String className = clazz.getName()")
            .addCode("\n")
            .beginControlFlow("switch (className)")
            
        // 添加内部接口的case
        innerInterfaces.forEach { (interfaceClass, implClass) ->
            methodBuilder.addCode(
                "case \$S:\n" +
                "    return new \$L();\n", 
                interfaceClass, implClass
            )
        }
        
        // 处理默认情况，返回null
        methodBuilder.addCode(
            "default:\n" +
            "    return null;\n"
        )
        
        methodBuilder.endControlFlow()
        
        return methodBuilder.build()
    }
} 