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
 * 映射关系通过 @ServiceAggregatorMethod 注解动态获取，支持多聚合接口。
 *
 * 生成产物：
 * - {ModuleName}_ServiceAggregator.kt（实现所有聚合接口）
 * - META-INF/services/{每个聚合接口}（SPI 注册文件）
 * - META-INF/service-registry/{module}.json（元数据，用于跨模块校验）
 * - META-INF/service-registry/{module}_report.txt（统计报告）
 *
 * 功能特性：
 * - priority 排序：数值越大越靠前，同优先级按类名字母序
 * - 跨模块 Key 重复检测：生成 JSON 元数据供 Gradle Task 汇总校验
 * - 编译期统计报告：输出注册数量摘要
 */
class ServiceAggregatorGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    private val options: Map<String, String>
) : BaseGeneration(codeGenerator, logger) {

    companion object {
        private val ANNOTATION_NAME = ServiceRegistry::class.qualifiedName!!
        private val METHOD_ANNOTATION_NAME = ServiceGroup::class.qualifiedName!!
    }

    private val grouped = mutableMapOf<String, MutableList<RegistrationInfo>>()
    private var serviceTypeMap = emptyMap<String, ServiceTypeInfo>()
    private val aggregatorInterfaces = mutableSetOf<String>()
    private var moduleName: String? = null

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
            logger.error("ServiceAggregator: Found @ServiceRegistry but no @ServiceAggregatorMethod discovered.")
            return emptyList()
        }

        for (decl in symbols) {
            if (moduleName == null) moduleName = extractModuleName(decl)

            val annotation = decl.annotations
                .firstOrNull { it.shortName.asString() == "ServiceRegistry" }
                ?: continue

            val targetInterface = annotation.arguments
                .firstOrNull { it.name?.asString() == "value" }?.value
                ?.let { (it as? KSType)?.declaration?.qualifiedName?.asString() }
                ?: continue

            val priority = annotation.arguments
                .firstOrNull { it.name?.asString() == "priority" }?.value as? Int ?: 0

            if (targetInterface in serviceTypeMap) {
                grouped.getOrPut(targetInterface) { mutableListOf() }
                    .add(RegistrationInfo(decl, priority))
            } else {
                logger.error("ServiceAggregator: @ServiceRegistry($targetInterface) unmatched. Class: ${decl.qualifiedName?.asString()}")
            }
        }
        return emptyList()
    }

    override fun hasDataToGenerate(): Boolean = grouped.isNotEmpty() && moduleName != null

    override fun generate() {
        val name = moduleName ?: return
        val className = "${name}_ServiceAggregator"
        val packageName = "com.webull.service"

        generateAggregatorClass(packageName, className)
        generateSpiFiles(packageName, className)
        generateMetadataJson(name)
        generateStatisticsReport(name)
        printStatisticsLog(name)
    }

    // ======================== 映射发现 ========================

    /**
     * 遍历源文件中的接口，查找带 @ServiceAggregatorMethod 的方法，建立映射。
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
                    val targetType = anno.arguments.firstOrNull()?.value as? KSType ?: continue
                    val targetInterface = targetType.declaration.qualifiedName?.asString() ?: continue

                    val params = func.parameters.map { p ->
                        (p.name?.asString() ?: "arg") to (p.type.resolve().declaration.qualifiedName?.asString() ?: "Any")
                    }
                    map[targetInterface] = ServiceTypeInfo(func.simpleName.asString(), targetInterface, params)
                }
                if (found) decl.qualifiedName?.asString()?.let { aggregatorInterfaces.add(it) }
            }
        }
        return map
    }

    // ======================== 代码生成 ========================

    private fun generateAggregatorClass(packageName: String, className: String) {
        if (aggregatorInterfaces.isEmpty()) return

        val typeSpec = TypeSpec.classBuilder(className)
        aggregatorInterfaces.sorted().forEach { typeSpec.addSuperinterface(ClassName.bestGuess(it)) }

        grouped.keys.sorted().forEach { interfaceName ->
            val registrations = grouped[interfaceName] ?: return@forEach
            val typeInfo = serviceTypeMap[interfaceName] ?: return@forEach

            // 按 priority 降序，同优先级按类名升序
            val sortedRegistrations = registrations.sortedWith(
                compareByDescending<RegistrationInfo> { it.priority }
                    .thenBy { it.declaration.qualifiedName?.asString() ?: "" }
            )

            val funSpec = FunSpec.builder(typeInfo.methodName)
                .addModifiers(KModifier.OVERRIDE)
                .returns(List::class.asClassName().parameterizedBy(ClassName.bestGuess(typeInfo.returnType)))

            // 参数签名
            val paramNames = typeInfo.parameters.map { (name, type) ->
                funSpec.addParameter(name, ClassName.bestGuess(type))
                name
            }

            // 有参数时校验构造函数
            if (paramNames.isNotEmpty()) validateConstructors(sortedRegistrations.map { it.declaration }, typeInfo)

            // 方法体
            val argsStr = paramNames.joinToString(", ")
            funSpec.addCode(buildCodeBlock {
                add("return listOf(\n")
                indent()
                sortedRegistrations.forEachIndexed { i, reg ->
                    val decl = reg.declaration
                    val implClass = ClassName(decl.packageName.asString(), decl.simpleName.asString())
                    val comma = if (i < sortedRegistrations.size - 1) "," else ""
                    when {
                        decl.classKind == ClassKind.OBJECT -> add("%T$comma\n", implClass)
                        paramNames.isEmpty() -> add("%T()$comma\n", implClass)
                        else -> add("%T($argsStr)$comma\n", implClass)
                    }
                }
                unindent()
                add(")\n")
            })

            typeSpec.addFunction(funSpec.build())
        }

        val fileSpec = FileSpec.builder(packageName, className)
            .addFileComment("Generated by ServiceAggregator KSP Processor. Do not modify!")
            .addType(typeSpec.build())
            .build()

        writeKotlinFile(fileSpec, buildDependencies(true, grouped.values.flatten().map { it.declaration }))
    }

    private fun generateSpiFiles(packageName: String, className: String) {
        aggregatorInterfaces.sorted().forEach { interfaceName ->
            codeGenerator.createNewFile(Dependencies(false), "META-INF.services", interfaceName, "")
                .apply { write("$packageName.$className\n".toByteArray()) }
                .close()
        }
    }

    // ======================== 跨模块元数据生成 ========================

    /**
     * 生成 JSON 元数据文件，供 Gradle Task 汇总进行跨模块 key 重复检测。
     * 输出路径：META-INF/service-registry/{module}.json
     */
    private fun generateMetadataJson(name: String) {
        val registrations = mutableListOf<String>()

        grouped.keys.sorted().forEach { interfaceName ->
            val regs = grouped[interfaceName] ?: return@forEach
            regs.sortedBy { it.declaration.qualifiedName?.asString() }.forEach { reg ->
                val decl = reg.declaration
                val className = decl.qualifiedName?.asString() ?: return@forEach
                val interfaceShortName = interfaceName.substringAfterLast(".")
                val key = tryExtractKey(decl)
                val keyField = if (key != null) ""","key":"$key"""" else ""
                registrations.add("""    {"class":"$className","interface":"$interfaceShortName","priority":${reg.priority}$keyField}""")
            }
        }

        val json = buildString {
            appendLine("{")
            appendLine("""  "module": "$name",""")
            appendLine("""  "registrations": [""")
            append(registrations.joinToString(",\n"))
            appendLine()
            appendLine("  ]")
            append("}")
        }

        codeGenerator.createNewFile(Dependencies(false), "META-INF.service-registry", name, "json")
            .apply { write(json.toByteArray()) }
            .close()
    }

    /**
     * 尝试从类声明中提取 key 属性的字符串常量值。
     * 仅支持简单的字符串字面量赋值（如 override val key = "xxx"）。
     */
    private fun tryExtractKey(decl: KSClassDeclaration): String? {
        // object 和 class 都可能有 key 属性
        val keyProp = decl.getAllProperties().firstOrNull { it.simpleName.asString() == "key" }
            ?: return null

        // KSP 无法直接读取属性初始值，但可以尝试从源码文本中提取
        // 这里返回 null 表示无法提取，依赖 @ServiceKey 注解或运行时检测
        // 未来可通过 @ServiceKey 注解显式声明
        return null
    }

    // ======================== 统计报告 ========================

    /**
     * 生成统计报告文件。
     * 输出路径：META-INF/service-registry/{module}_report.txt
     */
    private fun generateStatisticsReport(name: String) {
        val report = buildStatisticsText(name)

        codeGenerator.createNewFile(Dependencies(false), "META-INF.service-registry", "${name}_report", "txt")
            .apply { write(report.toByteArray()) }
            .close()
    }

    /**
     * 在编译日志中输出统计摘要。
     */
    private fun printStatisticsLog(name: String) {
        val report = buildStatisticsText(name)
        // 使用 warn 级别确保在编译输出中可见
        logger.warn(report)
    }

    private fun buildStatisticsText(name: String): String {
        val sb = StringBuilder()
        sb.appendLine("[ServiceAggregator] ═══ Module: $name ═══")

        var totalClasses = 0
        var totalObjects = 0

        grouped.keys.sorted().forEach { interfaceName ->
            val regs = grouped[interfaceName] ?: return@forEach
            val shortName = interfaceName.substringAfterLast(".")
            val objects = regs.count { it.declaration.classKind == ClassKind.OBJECT }
            val classes = regs.size - objects
            totalClasses += classes
            totalObjects += objects

            val priorityInfo = regs.filter { it.priority != 0 }
                .let { if (it.isNotEmpty()) ", ${it.size} with priority" else "" }

            sb.appendLine("  %-20s: %d registrations (%d objects, %d classes%s)".format(
                shortName, regs.size, objects, classes, priorityInfo
            ))
        }

        sb.appendLine("  ${"─".repeat(50)}")
        sb.appendLine("  Total: ${totalClasses + totalObjects} registrations (${totalObjects} objects, ${totalClasses} classes)")
        sb.appendLine("  Aggregator interfaces: ${aggregatorInterfaces.size}")

        return sb.toString()
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
                            "doesn't match @ServiceAggregatorMethod params($expected)"
                )
            }
        }
    }
}

/**
 * 注册信息，包含类声明和优先级。
 */
data class RegistrationInfo(
    val declaration: KSClassDeclaration,
    val priority: Int
)

data class ServiceTypeInfo(
    val methodName: String,
    val returnType: String,
    val parameters: List<Pair<String, String>> = emptyList()
)
