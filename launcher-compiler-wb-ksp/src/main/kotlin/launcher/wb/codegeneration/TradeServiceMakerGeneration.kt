package launcher.wb.codegeneration

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import launcher.TradeServiceMaker

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * 功能六：TradeServiceMaker 聚合接口 代码生成器（KotlinPoet 版本）。
 *
 * 编译时扫描指定包下所有继承自 baseInterface 的接口，分析继承关系，
 * 找出顶层大接口（不被其他接口继承的接口），自动生成一个聚合接口继承所有顶层接口。
 *
 * 处理流程：
 * 1. 收集 @TradeServiceMaker 注解的类
 * 2. 从注解获取 baseInterface、scanPackages、additionalInterfaces、packageName、className
 * 3. 通过 Resolver 扫描指定包下所有继承自 baseInterface 的接口
 * 4. 筛选出顶层接口（不被其他接口继承的）
 * 5. 生成聚合 interface，继承所有顶层接口 + additionalInterfaces
 *
 * 生成结构：
 * ```kotlin
 * package com.webull.commonmodule.trade.service
 *
 * interface ITradeManagerService :
 *     ITradeAccountInterface,
 *     ITradeOrderInterface,
 *     ITradePositionInterface,
 *     IService {
 * }
 * ```
 */
class TradeServiceMakerGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseGeneration(codeGenerator, logger) {

    /** 收集到的注解数据 */
    private val annotatedData = mutableListOf<TradeServiceMakerData>()

    /** 扫描结果：每个注解对应的顶层接口列表 */
    private val scanResults = mutableListOf<TradeServiceMakerScanResult>()

    /** isSubtypeOf 结果缓存：key = "qualifiedName -> baseQualifiedName" */
    private val subtypeCache = mutableMapOf<String, Boolean>()

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()

        resolver.getSymbolsWithAnnotation(TradeServiceMaker::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) {
                val data = extractAnnotationData(symbol)
                if (data != null) {
                    annotatedData += data
                    // 在 collect 阶段完成包扫描（Resolver 仅在 process 期间有效）
                    val allSubInterfaces = findAllSubInterfaces(data, resolver)
                    val topLevelInterfaces = filterTopLevelInterfaces(allSubInterfaces)
                    scanResults += TradeServiceMakerScanResult(data, topLevelInterfaces, allSubInterfaces)
                }
            }
        }

        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean = scanResults.isNotEmpty()

    override fun generate() {
        for (result in scanResults) {
            generateForResult(result)
        }
    }

    /**
     * 为单个扫描结果生成聚合接口
     */
    private fun generateForResult(result: TradeServiceMakerScanResult) {
        val data = result.data
        val name = data.targetClassName

        logger.info(
            "TradeServiceAggregator: Total interfaces found: ${result.allSubInterfaces.size}"
        )
        logger.info(
            "TradeServiceAggregator: Top-level interfaces: ${result.topLevelInterfaces.size}"
        )

        val fileSpec = brewKotlin(data, result.topLevelInterfaces)
        val sourceFiles = (result.allSubInterfaces + listOfNotNull(data.annotatedClass))
            .mapNotNull { it.containingFile }
        if (sourceFiles.isEmpty()) {
            logger.warn("TradeServiceAggregator: No source files for dependencies, incremental compilation may not work.")
        }
        writeKotlinFile(
            fileSpec = fileSpec,
            dependencies = Dependencies(aggregating = true, *sourceFiles.toTypedArray())
        )
    }

    // ======================== 注解数据提取 ========================

    /**
     * 从 @TradeServiceMaker 注解中提取所有参数
     */
    private fun extractAnnotationData(classDecl: KSClassDeclaration): TradeServiceMakerData? {
        val annotation = classDecl.annotations.firstOrNull {
            it.shortName.asString() == "TradeServiceMaker"
        } ?: return null

        // baseInterface（KClass 在 KSP 中返回 KSType）
        val baseInterfaceArg = annotation.arguments.firstOrNull { it.name?.asString() == "baseInterface" }
        val baseInterfaceType = baseInterfaceArg?.value as? KSType
        if (baseInterfaceType == null) {
            logger.error("TradeServiceAggregator: baseInterface is required", classDecl)
            return null
        }

        // scanPackages（Array<String>）
        @Suppress("UNCHECKED_CAST")
        val scanPackages = (annotation.arguments.firstOrNull {
            it.name?.asString() == "scanPackages"
        }?.value as? List<String>) ?: emptyList()

        if (scanPackages.isEmpty()) {
            logger.warn("TradeServiceAggregator: scanPackages is empty", classDecl)
        }

        // additionalInterfaces（Array<KClass<*>> 在 KSP 中返回 List<KSType>）
        @Suppress("UNCHECKED_CAST")
        val additionalInterfaces = (annotation.arguments.firstOrNull {
            it.name?.asString() == "additionalInterfaces"
        }?.value as? List<KSType>) ?: emptyList()

        // packageName
        val packageName = (annotation.arguments.firstOrNull {
            it.name?.asString() == "packageName"
        }?.value as? String)?.ifEmpty { null }
            ?: classDecl.packageName.asString()

        // className
        val className = (annotation.arguments.firstOrNull {
            it.name?.asString() == "className"
        }?.value as? String)?.ifEmpty { null }
            ?: "${classDecl.simpleName.asString()}Generated"

        return TradeServiceMakerData(
            annotatedClass = classDecl,
            baseInterfaceType = baseInterfaceType,
            scanPackages = scanPackages,
            additionalInterfaces = additionalInterfaces,
            targetPackageName = packageName,
            targetClassName = className
        )
    }

    // ======================== 包扫描 ========================

    /**
     * 查找所有继承自 baseInterface 的接口
     *
     * KSP 中通过 Resolver 获取指定包下的声明，
     * 然后过滤出接口并检查继承关系。
     */
    private fun findAllSubInterfaces(
        data: TradeServiceMakerData,
        resolver: Resolver
    ): List<KSClassDeclaration> {
        val baseDecl = data.baseInterfaceType.declaration as? KSClassDeclaration ?: return emptyList()
        val baseQualifiedName = baseDecl.qualifiedName?.asString() ?: return emptyList()
        val packagePrefixes = data.scanPackages.map { it.trim() }.filter { it.isNotEmpty() }

        if (packagePrefixes.isEmpty()) return emptyList()

        logger.info("TradeServiceAggregator: Scanning packages: $packagePrefixes")

        val result = mutableListOf<KSClassDeclaration>()

        // KSP 中通过 getDeclarationsFromPackage 获取包下的声明
        // 需要遍历每个包前缀，获取该包下的所有声明
        // 注意：getDeclarationsFromPackage 只获取精确包名的声明，不包含子包
        // 所以需要通过 getAllFiles 来扫描所有文件中的声明
        for (file in resolver.getAllFiles()) {
            val filePackage = file.packageName.asString()
            val matches = packagePrefixes.any { prefix ->
                filePackage == prefix || filePackage.startsWith("$prefix.")
            }
            if (!matches) continue

            for (declaration in file.declarations) {
                if (declaration is KSClassDeclaration &&
                    declaration.classKind == ClassKind.INTERFACE
                ) {
                    val declQualifiedName = declaration.qualifiedName?.asString() ?: continue
                    // 排除 baseInterface 自身
                    if (declQualifiedName == baseQualifiedName) continue

                    // 检查是否继承自 baseInterface
                    if (isSubtypeOf(declaration, baseQualifiedName)) {
                        result += declaration
                    }
                }
            }
        }

        logger.info(
            "TradeServiceAggregator: Found ${result.size} interfaces from file scanning"
        )

        return result.distinctBy { it.qualifiedName?.asString() }
    }

    /**
     * 检查接口是否直接或间接继承自指定的基础接口（带缓存）
     */
    private fun isSubtypeOf(declaration: KSClassDeclaration, baseQualifiedName: String): Boolean {
        val declName = declaration.qualifiedName?.asString() ?: return false
        val cacheKey = "$declName -> $baseQualifiedName"
        subtypeCache[cacheKey]?.let { return it }

        val result = declaration.getAllSuperTypes().any {
            it.declaration.qualifiedName?.asString() == baseQualifiedName
        }
        subtypeCache[cacheKey] = result
        return result
    }

    // ======================== 顶层接口筛选 ========================

    /**
     * 过滤出顶层大接口
     *
     * 规则：如果接口 A 被接口 B 继承（B extends A），且 A 和 B 都在候选列表中，
     * 那么 A 不是顶层接口（因为它已经被 B 包含了）
     */
    private fun filterTopLevelInterfaces(allInterfaces: List<KSClassDeclaration>): List<KSClassDeclaration> {
        val qualifiedNames = allInterfaces.map { it.qualifiedName?.asString() ?: "" }.toSet()
        val result = mutableListOf<KSClassDeclaration>()

        for (candidate in allInterfaces) {
            val candidateName = candidate.qualifiedName?.asString() ?: continue
            var isTopLevel = true

            for (other in allInterfaces) {
                if (other.qualifiedName?.asString() == candidateName) continue

                // 检查 other 是否直接或间接继承了 candidate
                if (extendsInterface(other, candidateName)) {
                    isTopLevel = false
                    break
                }
            }

            if (isTopLevel) {
                result += candidate
            }
        }

        return result
    }

    /**
     * 检查 subInterface 是否直接或间接继承了 superInterfaceName 指定的接口
     */
    private fun extendsInterface(subInterface: KSClassDeclaration, superInterfaceName: String): Boolean {
        // 遍历直接父接口
        for (superTypeRef in subInterface.superTypes) {
            val resolvedType = superTypeRef.resolve()
            val parentDecl = resolvedType.declaration as? KSClassDeclaration ?: continue
            val parentName = parentDecl.qualifiedName?.asString() ?: continue

            if (parentName == superInterfaceName) return true

            // 递归检查
            if (extendsInterface(parentDecl, superInterfaceName)) return true
        }
        return false
    }

    // ======================== 代码生成 ========================

    private fun brewKotlin(
        data: TradeServiceMakerData,
        topLevelInterfaces: List<KSClassDeclaration>
    ): FileSpec {
        return FileSpec.builder(data.targetPackageName, data.targetClassName)
            .addFileComment("Generated code from TradeServiceAggregator annotation processor!\n")
            .addFileComment("Do not modify!")
            .addType(createInterface(data, topLevelInterfaces))
            .build()
    }

    private fun createInterface(
        data: TradeServiceMakerData,
        topLevelInterfaces: List<KSClassDeclaration>
    ): TypeSpec {
        val interfaceBuilder = TypeSpec.interfaceBuilder(data.targetClassName)
            .addKdoc("交易模块整体对外接口\n")
            .addKdoc("自动生成，继承所有 Trade 模块的顶层接口\n")
            .addKdoc("\n")
            .addKdoc("继承的接口:\n")

        // 添加顶层接口（按简单名字母顺序排序，确保生成代码稳定）
        topLevelInterfaces
            .sortedBy { it.simpleName.asString() }
            .forEach { interfaceDecl ->
                val qualifiedName = interfaceDecl.qualifiedName?.asString() ?: return@forEach
                val className = ClassName.bestGuess(qualifiedName)
                interfaceBuilder.addSuperinterface(className)
                interfaceBuilder.addKdoc(" - %L\n", interfaceDecl.simpleName.asString())
            }

        // 添加额外的接口
        data.additionalInterfaces.forEach { additionalType ->
            val qualifiedName = additionalType.declaration.qualifiedName?.asString() ?: return@forEach
            val className = ClassName.bestGuess(qualifiedName)
            interfaceBuilder.addSuperinterface(className)
            interfaceBuilder.addKdoc(
                " - %L (additional)\n",
                additionalType.declaration.simpleName.asString()
            )
        }

        return interfaceBuilder.build()
    }

    // ======================== 数据类 ========================

    private data class TradeServiceMakerData(
        val annotatedClass: KSClassDeclaration,
        val baseInterfaceType: KSType,
        val scanPackages: List<String>,
        val additionalInterfaces: List<KSType>,
        val targetPackageName: String,
        val targetClassName: String
    )

    private data class TradeServiceMakerScanResult(
        val data: TradeServiceMakerData,
        val topLevelInterfaces: List<KSClassDeclaration>,
        val allSubInterfaces: List<KSClassDeclaration>
    )
}
