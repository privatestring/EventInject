package launcher.wb.mapper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * 注解解析器：负责解析 @Mapping、@Mappings 注解和 @InheritConfiguration 继承配置
 */
object AnnotationParser {

    /**
     * 收集方法上所有的 @Mapping 注解
     */
    fun collectMappings(method: KSFunctionDeclaration, logger: KSPLogger): List<MappingSpec> {
        val result = mutableListOf<MappingSpec>()

        // @Repeatable 的 @Mapping 直接返回多个
        method.annotations
            .filter { it.shortName.asString() == "Mapping" }
            .forEach { annotation ->
                parseMappingAnnotation(annotation, method, logger)?.let { result += it }
            }

        // @Mappings 容器注解
        method.annotations
            .filter { it.shortName.asString() == "Mappings" }
            .forEach { mappingsAnno ->
                @Suppress("UNCHECKED_CAST")
                val mappingList = mappingsAnno.getArgument("value") as? List<KSAnnotation> ?: emptyList()
                mappingList.forEach { annotation ->
                    parseMappingAnnotation(annotation, method, logger)?.let { result += it }
                }
            }

        return result
    }

    /**
     * 解析单个 @Mapping 注解为 MappingSpec
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

        if (expression != null && source != null) {
            logger.error("@Mapping cannot have both 'source' and 'expression' attributes.", method)
            return null
        }
        if (expression != null && constant != null) {
            logger.error("@Mapping cannot have both 'constant' and 'expression' attributes.", method)
            return null
        }

        return MappingSpec(target = target, source = source, constant = constant, expression = expression, ignore = ignore)
    }

    /**
     * 解析所有方法的继承映射配置（@InheritConfiguration）
     */
    fun resolveInheritedMappings(descriptor: MapperDescriptor, logger: KSPLogger) {
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
    fun KSAnnotation.getArgument(name: String): Any? {
        return arguments.firstOrNull { it.name?.asString() == name }?.value
    }
}
