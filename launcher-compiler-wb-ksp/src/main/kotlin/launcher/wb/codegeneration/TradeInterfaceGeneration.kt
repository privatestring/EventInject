package launcher.wb.codegeneration

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.buildCodeBlock

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * 功能五：TradeInterface 交易服务工厂 代码生成器（KotlinPoet 版本）。
 *
 * 收集所有 @TradeInterface 注解的实现类，生成 TradeInterfaceFactory{moduleName} class。
 * 运行时通过接口 Class 获取对应实现类实例，实现编译时的服务定位器模式。
 *
 * 需要编译参数 module_name，未配置时不执行。
 *
 * 生成结构：
 * ```kotlin
 * package com.webull.trade.services
 *
 * class TradeInterfaceFactory{ModuleName} : ITradeInterfaceFactory {
 *     override fun <T : ITradeInterface> createInstance(clazz: Class<out T>): ITradeInterface? { ... }
 *     private fun <T : ITradeInterface> createInnerInstance(clazz: Class<out T>): ITradeInterface? { ... }
 * }
 * ```
 */
class TradeInterfaceGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    private val options: Map<String, String>
) : BaseGeneration(codeGenerator, logger) {

    /** 普通接口映射：接口全限定名 → 实现类声明 */
    private val regularInterfaces = mutableMapOf<String, KSClassDeclaration>()

    /** 内部接口映射：接口全限定名 → 实现类声明 */
    private val innerInterfaces = mutableMapOf<String, KSClassDeclaration>()

    /** 模块名：优先使用 ksp arg 配置的 module_name，未配置则从源文件路径自动提取 */
    private var moduleName: String? = null

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        // 优先使用 ksp arg 配置的 module_name
        if (moduleName == null) {
            val configured = options["module_name"]
            if (!configured.isNullOrEmpty()) {
                moduleName = configured
            }
        }

        val unprocessed = mutableListOf<KSAnnotated>()

        resolver.getSymbolsWithAnnotation("launcher.TradeInterface").forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) {
                // 未通过 ksp arg 配置时，从源文件路径自动提取模块名
                if (moduleName == null) {
                    moduleName = extractModuleName(symbol)
                }
                processAnnotatedClass(symbol)
            }
        }

        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean =
        moduleName != null && (regularInterfaces.isNotEmpty() || innerInterfaces.isNotEmpty())

    override fun generate() {
        val name = moduleName ?: return
        val fileSpec = brewKotlin(name)
        val allClasses = (regularInterfaces.values + innerInterfaces.values).toList()
        writeKotlinFile(
            fileSpec = fileSpec,
            dependencies = buildDependencies(aggregating = true, allClasses)
        )
    }

    /**
     * 处理单个 @TradeInterface 标注的类。
     * KSP 中注解的 KClass 参数值以 KSType 形式返回，可直接获取。
     */
    private fun processAnnotatedClass(classDecl: KSClassDeclaration) {
        val annotation = classDecl.annotations.firstOrNull {
            it.shortName.asString() == "TradeInterface"
        } ?: return

        // 获取 value 参数（KClass<*> 在 KSP 中返回 KSType）
        val valueArg = annotation.arguments.firstOrNull { it.name?.asString() == "value" }
        val interfaceType = valueArg?.value as? KSType ?: run {
            logger.error("TradeInterface: cannot resolve 'value' parameter", classDecl)
            return
        }

        val interfaceClassName = interfaceType.declaration.qualifiedName?.asString() ?: run {
            logger.error("TradeInterface: cannot get qualified name of interface type", classDecl)
            return
        }

        // 获取 isInner 参数
        val isInnerArg = annotation.arguments.firstOrNull { it.name?.asString() == "isInner" }
        val isInner = isInnerArg?.value as? Boolean ?: false

        if (isInner) {
            innerInterfaces[interfaceClassName] = classDecl
        } else {
            regularInterfaces[interfaceClassName] = classDecl
        }
    }

    // ======================== 代码生成 ========================

    private fun brewKotlin(moduleName: String): FileSpec {
        val className = "TradeInterfaceFactory$moduleName"
        return FileSpec.builder(PACKAGE_NAME, className)
            .addFileComment("Generated code from TradeInterface! Do not modify.")
            .addType(createFactoryObject(className))
            .build()
    }

    private fun createFactoryObject(className: String): TypeSpec {
        return TypeSpec.classBuilder(className)
            .addKdoc("自动生成的 TradeInterfaceFactory 类\n由 TradeInterface 注解处理器生成\n")
            .addSuperinterface(I_TRADE_INTERFACE_FACTORY)
            .addFunction(buildCreateInstanceMethod())
            .addFunction(buildCreateInnerInstanceMethod())
            .build()
    }

    // ======================== 方法生成 ========================

    /**
     * 生成 createInstance 方法（override ITradeInterfaceFactory）
     * 使用 when 分支匹配普通接口，找不到则 fallback 到 createInnerInstance
     */
    private fun buildCreateInstanceMethod(): FunSpec {
        val typeVar = TypeVariableName("T", I_TRADE_INTERFACE)
        val paramType = ClassName("java.lang", "Class")
            .parameterizedBy(WildcardTypeName.producerOf(typeVar))

        return FunSpec.builder("createInstance")
            .addModifiers(KModifier.OVERRIDE)
            .addTypeVariable(typeVar)
            .addParameter("clazz", paramType)
            .returns(I_TRADE_INTERFACE.copy(nullable = true))
            .addCode(buildCodeBlock {
                addStatement("val className = clazz.name")
                beginControlFlow("return when (className)")
                regularInterfaces.forEach { (interfaceClass, implDecl) ->
                    val implClassName = ClassName.bestGuess(
                        implDecl.qualifiedName?.asString() ?: return@forEach
                    )
                    addStatement("%S -> %T()", interfaceClass, implClassName)
                }
                addStatement("else -> createInnerInstance(clazz)")
                endControlFlow()
            })
            .build()
    }

    /**
     * 生成 createInnerInstance 方法（private）
     * 使用 when 分支匹配内部接口，找不到返回 null
     */
    private fun buildCreateInnerInstanceMethod(): FunSpec {
        val typeVar = TypeVariableName("T", I_TRADE_INTERFACE)
        val paramType = ClassName("java.lang", "Class")
            .parameterizedBy(WildcardTypeName.producerOf(typeVar))

        return FunSpec.builder("createInnerInstance")
            .addModifiers(KModifier.PRIVATE)
            .addTypeVariable(typeVar)
            .addParameter("clazz", paramType)
            .returns(I_TRADE_INTERFACE.copy(nullable = true))
            .addCode(buildCodeBlock {
                addStatement("val className = clazz.name")
                beginControlFlow("return when (className)")
                innerInterfaces.forEach { (interfaceClass, implDecl) ->
                    val implClassName = ClassName.bestGuess(
                        implDecl.qualifiedName?.asString() ?: return@forEach
                    )
                    addStatement("%S -> %T()", interfaceClass, implClassName)
                }
                addStatement("else -> null")
                endControlFlow()
            })
            .build()
    }

    /**
     * 从 KSClassDeclaration 的源文件路径中提取模块名，并转为 PascalCase。
     * 路径格式约定：.../模块名/src/main/...
     * 例如：trade-order → TradeOrder, TradeModule → TradeModule
     */
    private fun extractModuleName(classDecl: KSClassDeclaration): String? {
        val filePath = classDecl.containingFile?.filePath ?: return null
        val srcIndex = filePath.indexOf("/src/")
        if (srcIndex > 0) {
            val beforeSrc = filePath.substring(0, srcIndex)
            val rawName = beforeSrc.substringAfterLast("/")
            return rawName.toPascalCase()
        }
        return null
    }

    /** 将模块名转为 PascalCase：trade-order → TradeOrder */
    private fun String.toPascalCase(): String {
        return split("-", "_")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    companion object {
        private const val PACKAGE_NAME = "com.webull.trade.services"

        private val I_TRADE_INTERFACE = ClassName(
            "com.webull.commonmodule.trade.service.trade.base", "ITradeInterface"
        )
        private val I_TRADE_INTERFACE_FACTORY = ClassName(
            "com.webull.commonmodule.trade.service.trade.base", "ITradeInterfaceFactory"
        )
    }
}
