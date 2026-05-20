package launcher.wb.codegeneration

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import wb.service.ServiceEntry
import wb.service.ServiceGroup
import wb.service.ServiceRegistry

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/20
 *
 * ServiceAggregator 代码生成器。
 *
 * 扫描 @ServiceRegistry 注解，按注解参数分组，生成模块级聚合类 + SPI 注册文件。
 *
 * 核心机制：
 * - 通过 @ServiceGroup 注解动态发现聚合接口与 SPI 接口的映射关系
 * - 通过方法返回类型自动判断生成模式：List<X> → eager，List<ServiceEntry<X>> → lazy
 *
 * 生成产物：
 * - {ModuleName}_ServiceAggregator.kt — 实现所有聚合接口
 * - META-INF/services/{聚合接口} — SPI 注册文件
 * - META-INF/service-registry/{module}.json — 跨模块校验元数据
 * - META-INF/service-registry/{module}_report.txt — 统计报告
 */
class ServiceAggregatorGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    private val options: Map<String, String>
) : BaseGeneration(codeGenerator, logger) {

    companion object {
        /** @ServiceRegistry 全限定名 */
        private val ANNOTATION_NAME = ServiceRegistry::class.qualifiedName!!
        /** @ServiceGroup 全限定名 */
        private val METHOD_ANNOTATION_NAME = ServiceGroup::class.qualifiedName!!
        /** ServiceEntry 全限定名，用于返回类型检测 */
        private val SERVICE_ENTRY_QUALIFIED = ServiceEntry::class.qualifiedName!!
        /** ServiceEntry 的 KotlinPoet ClassName */
        private val SERVICE_ENTRY_CLASS = ClassName.bestGuess(SERVICE_ENTRY_QUALIFIED)
    }

    /** 按目标接口分组的注册信息 */
    private val grouped = mutableMapOf<String, MutableList<RegistrationInfo>>()
    /** @ServiceGroup 方法映射表：目标接口 → 方法信息 */
    private var serviceTypeMap = emptyMap<String, ServiceTypeInfo>()
    /** 所有发现的聚合接口全限定名 */
    private val aggregatorInterfaces = mutableSetOf<String>()
    private var moduleName: String? = null

    // ======================== 收集阶段 ========================

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        if (moduleName == null) {
            options[OPTION_MODULE_NAME]?.takeIf { it.isNotEmpty() }?.let { moduleName = it }
        }
        if (serviceTypeMap.isEmpty()) {
            serviceTypeMap = buildServiceTypeMap(resolver)
        }

        val symbols = resolver.getSymbolsWithAnnotation(ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>().toList()

        if (symbols.isNotEmpty() && serviceTypeMap.isEmpty()) {
            // 第一轮可能还没发现 @ServiceGroup（多轮处理），延迟到下一轮
            logger.warn("ServiceAggregator: Found @ServiceRegistry but no @ServiceGroup yet, deferring.")
            return symbols
        }

        for (decl in symbols) {
            if (moduleName == null) moduleName = extractModuleName(decl)
            val (targetInterface, priority) = parseRegistryAnnotation(decl) ?: continue

            if (targetInterface in serviceTypeMap) {
                grouped.getOrPut(targetInterface) { mutableListOf() }.add(RegistrationInfo(decl, priority))
            } else {
                logger.error("ServiceAggregator: @ServiceRegistry($targetInterface) unmatched. Class: ${decl.qualifiedName?.asString()}")
            }
        }
        return emptyList()
    }

    override fun hasDataToGenerate(): Boolean = grouped.isNotEmpty() && moduleName != null

    // ======================== 生成阶段 ========================

    override fun generate() {
        val name = moduleName ?: return
        val className = "${name}_ServiceAggregator"
        val packageName = "com.webull.service"

        generateAggregatorClass(packageName, className)
        generateSpiFiles(packageName, className)
        generateMetadataJson(name)
        generateReport(name)
    }

    // ======================== 映射发现 ========================

    /**
     * 遍历源文件中的接口，查找带 @ServiceGroup 的方法，建立映射。
     * 同时通过方法返回类型判断 lazy 模式。
     */
    private fun buildServiceTypeMap(resolver: Resolver): Map<String, ServiceTypeInfo> {
        val map = mutableMapOf<String, ServiceTypeInfo>()

        for (file in resolver.getAllFiles()) {
            for (decl in file.declarations) {
                if (decl !is KSClassDeclaration || decl.classKind != ClassKind.INTERFACE) continue

                var found = false
                for (func in decl.getDeclaredFunctions()) {
                    val anno = func.annotations.firstOrNull { a ->
                        a.annotationType.resolve().declaration.qualifiedName?.asString() == METHOD_ANNOTATION_NAME
                    } ?: continue

                    found = true
                    val targetType = anno.arguments.firstOrNull { it.name?.asString() == "value" }?.value as? KSType ?: continue
                    val targetInterface = targetType.declaration.qualifiedName?.asString() ?: continue
                    val isLazy = isServiceEntryReturnType(func.returnType?.resolve())
                    val params = func.parameters.map { p ->
                        (p.name?.asString() ?: "arg") to (p.type.resolve().declaration.qualifiedName?.asString() ?: "Any")
                    }
                    map[targetInterface] = ServiceTypeInfo(func.simpleName.asString(), targetInterface, params, isLazy)
                }
                if (found) decl.qualifiedName?.asString()?.let { aggregatorInterfaces.add(it) }
            }
        }
        return map
    }

    /** 判断返回类型是否为 List<ServiceEntry<X>>，是则为 lazy 模式 */
    private fun isServiceEntryReturnType(returnType: KSType?): Boolean {
        if (returnType == null) return false
        val decl = returnType.declaration.qualifiedName?.asString()
        if (decl != "kotlin.collections.List" && decl != "java.util.List") return false
        val typeArg = returnType.arguments.firstOrNull()?.type?.resolve() ?: return false
        return typeArg.declaration.qualifiedName?.asString() == SERVICE_ENTRY_QUALIFIED
    }

    /** 解析 @ServiceRegistry 注解，返回 (目标接口, priority) */
    private fun parseRegistryAnnotation(decl: KSClassDeclaration): Pair<String, Int>? {
        val annotation = decl.annotations.firstOrNull { it.shortName.asString() == "ServiceRegistry" } ?: return null
        val targetInterface = annotation.arguments
            .firstOrNull { it.name?.asString() == "value" }?.value
            ?.let { (it as? KSType)?.declaration?.qualifiedName?.asString() } ?: return null
        val priority = annotation.arguments
            .firstOrNull { it.name?.asString() == "priority" }?.value as? Int ?: 0
        return targetInterface to priority
    }

    // ======================== 聚合类生成 ========================

    private fun generateAggregatorClass(packageName: String, className: String) {
        if (aggregatorInterfaces.isEmpty()) return

        val typeSpec = TypeSpec.classBuilder(className)
        aggregatorInterfaces.sorted().forEach { typeSpec.addSuperinterface(ClassName.bestGuess(it)) }

        for (interfaceName in grouped.keys.sorted()) {
            val registrations = grouped[interfaceName] ?: continue
            val typeInfo = serviceTypeMap[interfaceName] ?: continue
            val sorted = registrations.sortedWith(
                compareByDescending<RegistrationInfo> { it.priority }.thenBy { it.declaration.qualifiedName?.asString() ?: "" }
            )
            typeSpec.addFunction(buildMethodForGroup(typeInfo, sorted))
        }

        val fileSpec = FileSpec.builder(packageName, className)
            .addFileComment("Generated by ServiceAggregator KSP Processor. Do not modify!")
            .addType(typeSpec.build())
            .build()
        writeKotlinFile(fileSpec, buildDependencies(true, grouped.values.flatten().map { it.declaration }))
    }

    /** 根据 lazy/eager 模式构建对应方法 */
    private fun buildMethodForGroup(typeInfo: ServiceTypeInfo, registrations: List<RegistrationInfo>): FunSpec {
        val returnType = ClassName.bestGuess(typeInfo.returnType)
        val listElementType = if (typeInfo.isLazy) SERVICE_ENTRY_CLASS.parameterizedBy(returnType) else returnType

        val funSpec = FunSpec.builder(typeInfo.methodName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(List::class.asClassName().parameterizedBy(listElementType))

        val paramNames = typeInfo.parameters.map { (name, type) ->
            funSpec.addParameter(name, ClassName.bestGuess(type)); name
        }
        if (paramNames.isNotEmpty()) validateConstructors(registrations.map { it.declaration }, typeInfo)

        val argsStr = paramNames.joinToString(", ")
        funSpec.addCode(buildCodeBlock {
            add("return listOf(\n")
            indent()
            registrations.forEachIndexed { i, reg ->
                val implClass = ClassName(reg.declaration.packageName.asString(), reg.declaration.simpleName.asString())
                val comma = if (i < registrations.size - 1) ",\n" else "\n"
                add(formatRegistration(reg.declaration, implClass, argsStr, typeInfo.isLazy))
                add(comma)
            }
            unindent()
            add(")\n")
        })
        return funSpec.build()
    }

    /** 格式化单个注册项的代码表达式 */
    private fun formatRegistration(
        decl: KSClassDeclaration, implClass: ClassName, argsStr: String, isLazy: Boolean
    ): CodeBlock {
        val isObject = decl.classKind == ClassKind.OBJECT
        return if (isLazy) {
            val body = if (isObject) "%T" else if (argsStr.isEmpty()) "%T()" else "%T($argsStr)"
            CodeBlock.of("%T(%T::class.java) { $body }", SERVICE_ENTRY_CLASS, implClass, implClass)
        } else {
            when {
                isObject -> CodeBlock.of("%T", implClass)
                argsStr.isEmpty() -> CodeBlock.of("%T()", implClass)
                else -> CodeBlock.of("%T($argsStr)", implClass)
            }
        }
    }

    // ======================== SPI 文件 ========================

    private fun generateSpiFiles(packageName: String, className: String) {
        aggregatorInterfaces.sorted().forEach { interfaceName ->
            codeGenerator.createNewFile(Dependencies(false), "META-INF.services", interfaceName, "")
                .apply { write("$packageName.$className\n".toByteArray()) }.close()
        }
    }

    // ======================== 跨模块元数据 ========================

    private fun generateMetadataJson(name: String) {
        val entries = grouped.keys.sorted().flatMap { interfaceName ->
            val regs = grouped[interfaceName] ?: return@flatMap emptyList()
            regs.sortedBy { it.declaration.qualifiedName?.asString() }.map { reg ->
                val cls = reg.declaration.qualifiedName?.asString() ?: ""
                val iface = interfaceName.substringAfterLast(".")
                """    {"class":"$cls","interface":"$iface","priority":${reg.priority}}"""
            }
        }

        val json = buildString {
            appendLine("{")
            appendLine("""  "module": "$name",""")
            appendLine("""  "registrations": [""")
            append(entries.joinToString(",\n"))
            appendLine()
            appendLine("  ]")
            append("}")
        }

        codeGenerator.createNewFile(Dependencies(false), "META-INF.service-registry", name, "json")
            .apply { write(json.toByteArray()) }.close()
    }

    // ======================== 统计报告 ========================

    private fun generateReport(name: String) {
        val report = buildReport(name)
        logger.warn(report)
    }

    private fun buildReport(name: String): String = buildString {
        appendLine("[ServiceAggregator] ═══ Module: $name ═══")
        var total = 0
        var totalObjects = 0

        grouped.keys.sorted().forEach { interfaceName ->
            val regs = grouped[interfaceName] ?: return@forEach
            val typeInfo = serviceTypeMap[interfaceName]
            val objects = regs.count { it.declaration.classKind == ClassKind.OBJECT }
            total += regs.size
            totalObjects += objects

            val tags = mutableListOf<String>()
            tags += "${objects} objects"
            tags += "${regs.size - objects} classes"
            regs.count { it.priority != 0 }.takeIf { it > 0 }?.let { tags += "$it with priority" }
            if (typeInfo?.isLazy == true) tags += "lazy"

            appendLine("  %-20s: %d registrations (%s)".format(
                interfaceName.substringAfterLast("."), regs.size, tags.joinToString(", ")
            ))
        }
        appendLine("  ${"─".repeat(50)}")
        appendLine("  Total: $total registrations ($totalObjects objects, ${total - totalObjects} classes)")
        appendLine("  Aggregator interfaces: ${aggregatorInterfaces.size}")
    }

    // ======================== 校验 ========================

    private fun validateConstructors(impls: List<KSClassDeclaration>, typeInfo: ServiceTypeInfo) {
        val expected = typeInfo.parameters.map { it.second }
        for (decl in impls) {
            if (decl.classKind == ClassKind.OBJECT) continue
            val actual = decl.primaryConstructor?.parameters?.map {
                it.type.resolve().declaration.qualifiedName?.asString() ?: "Any"
            } ?: emptyList()
            if (actual != expected) {
                logger.error(
                    "ServiceAggregator: ${decl.qualifiedName?.asString()} constructor($actual) " +
                            "doesn't match @ServiceGroup params($expected)"
                )
            }
        }
    }
}

/** 注册信息：类声明 + 优先级 */
data class RegistrationInfo(val declaration: KSClassDeclaration, val priority: Int)

/** @ServiceGroup 方法信息：方法名、返回类型、参数列表、是否 lazy */
data class ServiceTypeInfo(
    val methodName: String,
    val returnType: String,
    val parameters: List<Pair<String, String>> = emptyList(),
    val isLazy: Boolean = false
)
