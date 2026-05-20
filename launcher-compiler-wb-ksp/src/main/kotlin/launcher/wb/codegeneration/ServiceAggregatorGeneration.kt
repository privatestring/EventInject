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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
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

    /** 外部注册的 Provider（由其他 Generation 生成的类，无 KSClassDeclaration） */
    private val externalRegistrations = mutableMapOf<String, MutableList<ExternalRegistrationInfo>>()

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

        // 每轮都尝试补充映射（增量编译时新文件可能在后续轮次出现）
        val newMap = buildServiceTypeMap(resolver)
        if (newMap.isNotEmpty()) {
            serviceTypeMap = serviceTypeMap + newMap
        }

        val symbols = resolver.getSymbolsWithAnnotation(ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>().toList()

        if (symbols.isNotEmpty() && serviceTypeMap.isEmpty()) {
            logger.error("ServiceAggregator: Found @ServiceRegistry but no @ServiceGroup discovered.")
            return emptyList()
        }

        for (decl in symbols) {
            if (moduleName == null) moduleName = extractModuleName(decl)
            val registrations = parseRegistryAnnotations(decl)
            if (registrations.isEmpty()) continue

            for ((targetInterface, priority) in registrations) {
                if (targetInterface in serviceTypeMap) {
                    grouped.getOrPut(targetInterface) { mutableListOf() }.add(RegistrationInfo(decl, priority))
                } else {
                    logger.error("ServiceAggregator: @ServiceRegistry($targetInterface) unmatched. Class: ${decl.qualifiedName?.asString()}")
                }
            }
        }
        return emptyList()
    }

    override fun hasDataToGenerate(): Boolean = (grouped.isNotEmpty() || externalRegistrations.isNotEmpty()) && moduleName != null

    /**
     * 外部 Generation 调用此方法注册生成的 Provider 类到 ServiceAggregator。
     *
     * @param targetInterface 目标接口全限定名（如 "wb.service.IProvider"）
     * @param providerClassName 生成的 Provider 类的 KotlinPoet ClassName
     * @param sourceClassDecl 触发注册的源类声明，用于在模块无 @ServiceRegistry 时提取 moduleName
     */
    fun addExternalRegistration(targetInterface: String, providerClassName: ClassName, sourceClassDecl: KSClassDeclaration? = null) {
        externalRegistrations.getOrPut(targetInterface) { mutableListOf() }
            .add(ExternalRegistrationInfo(providerClassName))
        // 当模块没有 @ServiceRegistry 时，moduleName 为 null，需要从外部注册的源类中提取
        if (moduleName == null && sourceClassDecl != null) {
            moduleName = extractModuleName(sourceClassDecl)
        }
    }

    // ======================== 生成阶段 ========================

    override fun generate() {
        val name = moduleName ?: return
        val className = "${name}_ServiceAggregator"
        val packageName = "com.webull.service"

        generateAggregatorClass(packageName, className)
        generateSpiFiles(packageName, className)
        generateReport(name)
    }

    // ======================== 映射发现 ========================

    /**
     * 查找带 @ServiceGroup 的接口方法，建立映射。
     *
     * 两种发现方式：
     * 1. 遍历当前模块源文件（处理本模块定义的聚合接口）
     * 2. 从 classpath 按全限定名加载已知聚合接口（处理依赖模块中定义的接口，
     *    如 CoreModule 中的 IServiceAggregator）
     *
     * 同时通过方法返回类型判断 lazy 模式。
     */
    private fun buildServiceTypeMap(resolver: Resolver): Map<String, ServiceTypeInfo> {
        val map = mutableMapOf<String, ServiceTypeInfo>()

        // 方式一：遍历当前模块源文件
        for (file in resolver.getAllFiles()) {
            for (decl in file.declarations) {
                if (decl !is KSClassDeclaration || decl.classKind != ClassKind.INTERFACE) continue
                processInterfaceForServiceGroup(decl, map)
            }
        }

        // 方式二：从 classpath 加载已知的聚合接口（解决依赖模块中定义的接口不在 getAllFiles() 中的问题）
        if (map.isEmpty()) {
            val aggregatorInterface = resolver.getClassDeclarationByName(
                resolver.getKSNameFromString("com.webull.core.framework.service.IServiceAggregator")
            )
            if (aggregatorInterface != null) {
                processInterfaceForServiceGroup(aggregatorInterface, map)
            }
        }

        return map
    }

    /** 从接口声明中提取 @ServiceGroup 方法映射 */
    private fun processInterfaceForServiceGroup(
        decl: KSClassDeclaration,
        map: MutableMap<String, ServiceTypeInfo>
    ) {
        var found = false
        for (func in decl.getDeclaredFunctions()) {
            val anno = func.annotations.firstOrNull { a ->
                a.annotationType.resolve().declaration.qualifiedName?.asString() == METHOD_ANNOTATION_NAME
            } ?: continue

            found = true
            // @ServiceGroup 只有一个参数 value，直接取第一个
            val targetType = anno.arguments.firstOrNull()?.value as? KSType ?: continue
            val targetInterface = targetType.declaration.qualifiedName?.asString() ?: continue

            // 通过返回类型判断 lazy 模式，解析失败时默认 eager
            val isLazy = runCatching { isServiceEntryReturnType(func.returnType?.resolve()) }.getOrDefault(false)

            val params = func.parameters.map { p ->
                (p.name?.asString() ?: "arg") to (p.type.resolve().declaration.qualifiedName?.asString() ?: "Any")
            }
            map[targetInterface] = ServiceTypeInfo(func.simpleName.asString(), targetInterface, params, isLazy)
        }
        if (found) decl.qualifiedName?.asString()?.let { aggregatorInterfaces.add(it) }
    }

    /** 判断返回类型是否为 List<ServiceEntry<X>>，是则为 lazy 模式 */
    private fun isServiceEntryReturnType(returnType: KSType?): Boolean {
        if (returnType == null) return false
        val decl = returnType.declaration.qualifiedName?.asString() ?: return false
        if (decl != List::class.qualifiedName && decl != java.util.List::class.java.name) return false
        val typeArg = returnType.arguments.firstOrNull()?.type?.resolve() ?: return false
        val typeArgName = typeArg.declaration.qualifiedName?.asString() ?: return false
        return typeArgName == SERVICE_ENTRY_QUALIFIED
    }

    /** 解析所有 @ServiceRegistry 注解，返回 [(目标接口, priority)]（支持 @Repeatable） */
    private fun parseRegistryAnnotations(decl: KSClassDeclaration): List<Pair<String, Int>> {
        return decl.annotations
            .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION_NAME }
            .mapNotNull { annotation ->
                val targetInterface = annotation.arguments
                    .firstOrNull { it.name?.asString() == ServiceRegistry::value.name }?.value
                    ?.let { (it as? KSType)?.declaration?.qualifiedName?.asString() } ?: return@mapNotNull null
                val priority = annotation.arguments
                    .firstOrNull { it.name?.asString() == ServiceRegistry::priority.name }?.value as? Int ?: 0
                targetInterface to priority
            }.toList()
    }

    // ======================== 聚合类生成 ========================

    private fun generateAggregatorClass(packageName: String, className: String) {
        if (aggregatorInterfaces.isEmpty()) {
            if (externalRegistrations.isNotEmpty()) {
                logger.warn("ServiceAggregator: aggregatorInterfaces is empty but externalRegistrations has ${externalRegistrations.size} entries. " +
                        "IServiceAggregator may not be in classpath. External providers won't be registered.")
            }
            return
        }

        val typeSpec = TypeSpec.classBuilder(className)
        aggregatorInterfaces.sorted().forEach { typeSpec.addSuperinterface(ClassName.bestGuess(it)) }

        // 合并 grouped 和 externalRegistrations 的所有目标接口
        val allTargetInterfaces = (grouped.keys + externalRegistrations.keys).toSortedSet()

        for (interfaceName in allTargetInterfaces) {
            val registrations = grouped[interfaceName].orEmpty()
            val externals = externalRegistrations[interfaceName].orEmpty()
            val typeInfo = serviceTypeMap[interfaceName] ?: continue

            val sorted = registrations.sortedWith(
                compareByDescending<RegistrationInfo> { it.priority }.thenBy { it.declaration.qualifiedName?.asString() ?: "" }
            )
            typeSpec.addFunction(buildMethodForGroup(typeInfo, sorted, externals))
        }

        val fileSpec = FileSpec.builder(packageName, className)
            .addFileComment("Generated by ServiceAggregator KSP Processor. Do not modify!")
            .addType(typeSpec.build())
            .build()
        writeKotlinFile(fileSpec, buildDependencies(true, grouped.values.flatten().map { it.declaration }))
    }

    /** 根据 lazy/eager 模式构建对应方法 */
    private fun buildMethodForGroup(
        typeInfo: ServiceTypeInfo,
        registrations: List<RegistrationInfo>,
        externals: List<ExternalRegistrationInfo> = emptyList()
    ): FunSpec {
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
        val totalCount = registrations.size + externals.size
        funSpec.addCode(buildCodeBlock {
            add("return listOf(\n")
            indent()
            registrations.forEachIndexed { i, reg ->
                val implClass = reg.declaration.toClassName()
                val isLast = i == registrations.size - 1 && externals.isEmpty()
                val comma = if (isLast) "\n" else ",\n"
                add(formatRegistration(reg.declaration, implClass, argsStr, typeInfo.isLazy))
                add(comma)
            }
            externals.forEachIndexed { i, ext ->
                val isLast = i == externals.size - 1
                val comma = if (isLast) "\n" else ",\n"
                if (typeInfo.isLazy) {
                    add("%T(%T::class.java) { %T() }", SERVICE_ENTRY_CLASS, ext.className, ext.className)
                } else {
                    add("%T()", ext.className)
                }
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

    // ======================== 统计报告 ========================

    private fun generateReport(name: String) {
        val report = buildReport(name)
        logger.info(report)
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

/** 外部注册信息：由其他 Generation 生成的类，只有 ClassName */
data class ExternalRegistrationInfo(val className: ClassName)

/** @ServiceGroup 方法信息：方法名、返回类型、参数列表、是否 lazy */
data class ServiceTypeInfo(
    val methodName: String,
    val returnType: String,
    val parameters: List<Pair<String, String>> = emptyList(),
    val isLazy: Boolean = false
)
