package launcher.wb.mapper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * Mapper 核心逻辑工具类（KSP 版本）。
 * 负责构建 MapperDescriptor、解析映射规则、处理继承配置等。
 */
object MapperUtils {

    /**
     * 处理单个 Mapper 接口/抽象类，构建描述符
     */
    fun handleMapper(
        mapperElement: KSClassDeclaration,
        propertyResolver: PropertyResolver,
        logger: KSPLogger
    ): MapperDescriptor? {
        // 验证：@Mapper 只能应用于接口或抽象类
        if (mapperElement.classKind != ClassKind.INTERFACE &&
            !mapperElement.modifiers.contains(Modifier.ABSTRACT)
        ) {
            logger.error("@Mapper can only be applied to interface or abstract class.", mapperElement)
            return null
        }

        return buildMapperDescriptor(mapperElement, propertyResolver, logger)
    }

    /**
     * 构建 Mapper 描述符
     */
    private fun buildMapperDescriptor(
        mapperElement: KSClassDeclaration,
        propertyResolver: PropertyResolver,
        logger: KSPLogger
    ): MapperDescriptor? {
        // 获取 @Mapper 注解
        val mapperAnno = mapperElement.annotations.firstOrNull {
            it.shortName.asString() == "Mapper"
        }
        val suffix = mapperAnno?.getArgument("implementationSuffix") as? String ?: "Impl"

        // 获取 @MappingConfig 注解
        val mappingConfig = mapperElement.annotations.firstOrNull {
            it.shortName.asString() == "MappingConfig"
        }
        val needNullCheck = mappingConfig?.getArgument("isNeedNullCheck") as? Boolean ?: false

        // 确定包名和实现类名
        val packageName = mapperElement.packageName.asString()
        val implName = mapperElement.simpleName.asString() + suffix

        // 扫描所有方法
        val allMethods = mapperElement.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        logger.info("[Mapper] Found ${allMethods.size} methods in ${mapperElement.simpleName.asString()}")

        // 过滤出可处理的抽象方法
        val processableMethods = allMethods.filter { method ->
            isProcessableMethod(mapperElement, method)
        }

        // 构建方法描述符
        val methodDescriptors = processableMethods.mapNotNull { method ->
            buildMethodDescriptor(method, propertyResolver, logger)
        }

        if (methodDescriptors.isEmpty()) {
            logger.error("No abstract mapping methods found inside @Mapper type.", mapperElement)
            return null
        }

        // 收集 @BeforeMapping 和 @AfterMapping 方法
        val beforeMappingMethods = allMethods.filter { method ->
            method.annotations.any { it.shortName.asString() == "BeforeMapping" }
        }
        val afterMappingMethods = allMethods.filter { method ->
            method.annotations.any { it.shortName.asString() == "AfterMapping" }
        }

        // 收集 @MappingIgnore 方法
        val ignoredMethods = allMethods.filter { method ->
            method.annotations.any { it.shortName.asString() == "MappingIgnore" }
        }

        // 检查哪些 @MappingIgnore 方法被 expression 引用
        val referencedIgnoredMethods = ignoredMethods.filter { ignoredMethod ->
            val methodName = ignoredMethod.simpleName.asString()
            methodDescriptors.any { methodDesc ->
                methodDesc.ownMappings.any { mapping ->
                    val expr = mapping.expression?.trim() ?: ""
                    val processedExpr = if (expr.startsWith("java(") && expr.endsWith(")")) {
                        expr.substring(5, expr.length - 1)
                    } else {
                        expr
                    }
                    processedExpr.contains("$methodName(")
                }
            }
        }

        // 检测源文件类型
        val isKotlinSource = mapperElement.origin == Origin.KOTLIN ||
                mapperElement.origin == Origin.KOTLIN_LIB

        val descriptor = MapperDescriptor(
            mapperElement = mapperElement,
            packageName = packageName,
            implementationName = implName,
            methods = methodDescriptors,
            needNullCheck = needNullCheck,
            beforeMappingMethods = beforeMappingMethods,
            afterMappingMethods = afterMappingMethods,
            ignoredMethods = referencedIgnoredMethods,
            isKotlinSource = isKotlinSource
        )

        // 解析继承的映射配置
        resolveInheritedMappings(descriptor, logger)

        return descriptor
    }

    /**
     * 判断方法是否可处理（需要生成实现）
     */
    private fun isProcessableMethod(
        mapperElement: KSClassDeclaration,
        method: KSFunctionDeclaration
    ): Boolean {
        // 排除 @BeforeMapping 方法
        if (method.annotations.any { it.shortName.asString() == "BeforeMapping" }) return false
        // 排除 @AfterMapping 方法
        if (method.annotations.any { it.shortName.asString() == "AfterMapping" }) return false
        // 排除 @MappingIgnore 方法
        if (method.annotations.any { it.shortName.asString() == "MappingIgnore" }) return false

        return when {
            mapperElement.classKind == ClassKind.INTERFACE -> {
                // 接口中：必须是抽象方法（没有方法体）
                method.isAbstract
            }
            else -> {
                // 抽象类中：必须是 abstract 方法
                method.modifiers.contains(Modifier.ABSTRACT)
            }
        }
    }

    /**
     * 构建单个映射方法的描述符
     */
    private fun buildMethodDescriptor(
        method: KSFunctionDeclaration,
        propertyResolver: PropertyResolver,
        logger: KSPLogger
    ): MapperMethodDescriptor? {
        // 构建参数描述符
        val params = method.parameters.map { buildParameterDescriptor(it, propertyResolver) }

        // 查找 @MappingTarget 参数
        val mappingTargets = params.filter { it.isMappingTarget }
        if (mappingTargets.size > 1) {
            logger.error("Only one @MappingTarget parameter is supported.", method)
            return null
        }

        val mappingTarget = mappingTargets.firstOrNull()
        val primarySource = params.firstOrNull { !it.isMappingTarget }

        // 验证：必须有源参数
        if (primarySource == null) {
            logger.error(
                "Mapper method must declare at least one source parameter. Method: ${method.simpleName.asString()}",
                method
            )
            return null
        }

        // 验证：非更新方法不能返回 void/Unit
        val returnType = method.returnType?.resolve()
        if (mappingTarget == null && returnType != null) {
            val returnTypeName = returnType.declaration.qualifiedName?.asString()
            if (returnTypeName == "kotlin.Unit" || returnTypeName == "java.lang.Void") {
                logger.error(
                    "Non update mapper method must return a target type. Method: ${method.simpleName.asString()}",
                    method
                )
                return null
            }
        }

        // 收集 @Mapping 注解
        val ownMappings = collectMappings(method, logger)

        // 检查 @InheritConfiguration
        val inheritFrom = method.annotations
            .firstOrNull { it.shortName.asString() == "InheritConfiguration" }
            ?.getArgument("name") as? String

        // 检查方法级 @MappingConfig
        val methodMappingConfig = method.annotations
            .firstOrNull { it.shortName.asString() == "MappingConfig" }
        val methodNeedNullCheck = methodMappingConfig?.getArgument("isNeedNullCheck") as? Boolean

        return MapperMethodDescriptor(
            element = method,
            name = method.simpleName.asString(),
            returnType = returnType ?: return null,
            parameters = params,
            mappingTarget = mappingTarget,
            primarySource = primarySource,
            ownMappings = ownMappings,
            inheritFrom = inheritFrom?.takeIf { it.isNotBlank() },
            needNullCheck = methodNeedNullCheck
        )
    }

    /**
     * 构建参数描述符
     */
    private fun buildParameterDescriptor(
        param: KSValueParameter,
        propertyResolver: PropertyResolver
    ): ParameterDescriptor {
        val name = param.name?.asString() ?: "param"
        val isTarget = param.annotations.any { it.shortName.asString() == "MappingTarget" }
        val type = param.type.resolve()
        val typeDeclaration = propertyResolver.asClassDeclaration(type)
        return ParameterDescriptor(param, name, type, typeDeclaration, isTarget)
    }

    /**
     * 收集方法上所有的 @Mapping 注解
     */
    private fun collectMappings(
        method: KSFunctionDeclaration,
        logger: KSPLogger
    ): List<MappingSpec> {
        val result = mutableListOf<MappingSpec>()

        // 收集 @Mapping 注解（@Repeatable 在 KSP 中直接返回多个）
        method.annotations
            .filter { it.shortName.asString() == "Mapping" }
            .forEach { annotation ->
                val spec = parseMappingAnnotation(annotation, method, logger)
                if (spec != null) result += spec
            }

        // 收集 @Mappings 容器注解中的 @Mapping
        method.annotations
            .filter { it.shortName.asString() == "Mappings" }
            .forEach { mappingsAnno ->
                @Suppress("UNCHECKED_CAST")
                val mappingList = mappingsAnno.getArgument("value") as? List<KSAnnotation> ?: emptyList()
                mappingList.forEach { annotation ->
                    val spec = parseMappingAnnotation(annotation, method, logger)
                    if (spec != null) result += spec
                }
            }

        return result
    }

    /**
     * 解析单个 @Mapping 注解
     */
    private fun parseMappingAnnotation(
        annotation: KSAnnotation,
        method: KSFunctionDeclaration,
        logger: KSPLogger
    ): MappingSpec? {
        val target = (annotation.getArgument("target") as? String)?.takeIf { it.isNotBlank() }
        if (target == null) {
            logger.error("@Mapping target cannot be empty.", method)
            return null
        }

        val source = (annotation.getArgument("source") as? String)?.takeIf { it.isNotBlank() }
        val expression = (annotation.getArgument("expression") as? String)?.takeIf { it.isNotBlank() }
        val constant = (annotation.getArgument("constant") as? String)?.takeIf { it.isNotBlank() }
        val ignore = annotation.getArgument("ignore") as? Boolean ?: false

        // 验证：expression 和 source 不能同时使用
        if (expression != null && source != null) {
            logger.error("@Mapping cannot have both 'source' and 'expression' attributes.", method)
            return null
        }
        // 验证：expression 和 constant 不能同时使用
        if (expression != null && constant != null) {
            logger.error("@Mapping cannot have both 'constant' and 'expression' attributes.", method)
            return null
        }

        return MappingSpec(
            target = target,
            source = source,
            constant = constant,
            expression = expression,
            ignore = ignore
        )
    }

    /**
     * 解析所有方法的继承映射配置
     */
    private fun resolveInheritedMappings(descriptor: MapperDescriptor, logger: KSPLogger) {
        val methodMap = descriptor.methods.associateBy { it.name }
        descriptor.methods.forEach { method ->
            if (method.resolvedMappings.isEmpty()) {
                method.resolvedMappings = resolveMappingsForMethod(method, methodMap, mutableSetOf(), logger)
            }
        }
    }

    /**
     * 递归解析方法的映射配置（包括继承的配置）
     */
    private fun resolveMappingsForMethod(
        method: MapperMethodDescriptor,
        methods: Map<String, MapperMethodDescriptor>,
        visiting: MutableSet<String>,
        logger: KSPLogger
    ): List<MappingSpec> {
        val inherited = method.inheritFrom?.let { inheritName ->
            val inheritedMethod = methods[inheritName]
            if (inheritedMethod == null) {
                logger.error("@InheritConfiguration refers to unknown method $inheritName", method.element)
                emptyList()
            } else {
                if (!visiting.add(method.name)) {
                    logger.error("Circular @InheritConfiguration detected at ${method.name}", method.element)
                    emptyList()
                } else {
                    val resolved = if (inheritedMethod.resolvedMappings.isEmpty()) {
                        resolveMappingsForMethod(inheritedMethod, methods, visiting, logger)
                    } else {
                        inheritedMethod.resolvedMappings
                    }
                    visiting.remove(method.name)
                    resolved
                }
            }
        } ?: emptyList()

        // 合并：继承规则 + 当前方法规则，同一 target 取最后一个
        return (inherited + method.ownMappings)
            .groupBy { it.target }
            .map { (_, specs) -> specs.last() }
    }

    /**
     * KSAnnotation 扩展：获取指定名称的参数值
     */
    private fun KSAnnotation.getArgument(name: String): Any? {
        return arguments.firstOrNull { it.name?.asString() == name }?.value
    }
}
