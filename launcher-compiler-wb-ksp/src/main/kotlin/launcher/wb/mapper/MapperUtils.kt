package launcher.wb.mapper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import launcher.wb.mapper.AnnotationParser.getArgument

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * Mapper 描述符构建器：负责验证 Mapper 元素、构建 MapperDescriptor。
 * 注解解析和继承配置委托给 AnnotationParser。
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
        if (mapperElement.classKind != ClassKind.INTERFACE &&
            !mapperElement.modifiers.contains(Modifier.ABSTRACT)
        ) {
            logger.error("@Mapper can only be applied to interface or abstract class.", mapperElement)
            return null
        }

        return buildMapperDescriptor(mapperElement, propertyResolver, logger)
    }

    private fun buildMapperDescriptor(
        mapperElement: KSClassDeclaration,
        propertyResolver: PropertyResolver,
        logger: KSPLogger
    ): MapperDescriptor? {
        // 读取 @Mapper 注解配置
        val mapperAnno = mapperElement.annotations.firstOrNull { it.shortName.asString() == "Mapper" }
        val suffix = mapperAnno?.getArgument("implementationSuffix") as? String ?: "Impl"

        // 读取 @MappingConfig 注解配置
        val mappingConfig = mapperElement.annotations.firstOrNull { it.shortName.asString() == "MappingConfig" }
        val needNullCheck = mappingConfig?.getArgument("isNeedNullCheck") as? Boolean ?: false

        val packageName = mapperElement.packageName.asString()
        val implName = mapperElement.simpleName.asString() + suffix

        // 扫描所有方法
        val allMethods = mapperElement.declarations.filterIsInstance<KSFunctionDeclaration>().toList()
        logger.info("[Mapper] Found ${allMethods.size} methods in ${mapperElement.simpleName.asString()}")

        // 过滤可处理的抽象方法并构建描述符
        val methodDescriptors = allMethods
            .filter { isProcessableMethod(mapperElement, it) }
            .mapNotNull { buildMethodDescriptor(it, propertyResolver, logger) }

        if (methodDescriptors.isEmpty()) {
            logger.error("No abstract mapping methods found inside @Mapper type.", mapperElement)
            return null
        }

        // 收集生命周期方法
        val beforeMappingMethods = allMethods.filter { it.hasAnnotation("BeforeMapping") }
        val afterMappingMethods = allMethods.filter { it.hasAnnotation("AfterMapping") }

        // 收集被 expression 引用的 @MappingIgnore 方法
        val ignoredMethods = allMethods.filter { it.hasAnnotation("MappingIgnore") }
        val referencedIgnoredMethods = ignoredMethods.filter { ignoredMethod ->
            val methodName = ignoredMethod.simpleName.asString()
            methodDescriptors.any { desc ->
                desc.ownMappings.any { mapping ->
                    val expr = mapping.expression?.trim() ?: ""
                    val processed = if (expr.startsWith("java(") && expr.endsWith(")")) expr.substring(5, expr.length - 1) else expr
                    processed.contains("$methodName(")
                }
            }
        }

        val isKotlinSource = mapperElement.origin == Origin.KOTLIN || mapperElement.origin == Origin.KOTLIN_LIB

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

        // 解析继承配置
        AnnotationParser.resolveInheritedMappings(descriptor, logger)
        return descriptor
    }

    private fun isProcessableMethod(mapperElement: KSClassDeclaration, method: KSFunctionDeclaration): Boolean {
        if (method.hasAnnotation("BeforeMapping")) return false
        if (method.hasAnnotation("AfterMapping")) return false
        if (method.hasAnnotation("MappingIgnore")) return false

        return when {
            mapperElement.classKind == ClassKind.INTERFACE -> method.isAbstract
            else -> method.modifiers.contains(Modifier.ABSTRACT)
        }
    }

    private fun buildMethodDescriptor(
        method: KSFunctionDeclaration,
        propertyResolver: PropertyResolver,
        logger: KSPLogger
    ): MapperMethodDescriptor? {
        val params = method.parameters.map { buildParameterDescriptor(it, propertyResolver) }

        val mappingTargets = params.filter { it.isMappingTarget }
        if (mappingTargets.size > 1) {
            logger.error("Only one @MappingTarget parameter is supported.", method)
            return null
        }

        val mappingTarget = mappingTargets.firstOrNull()
        val primarySource = params.firstOrNull { !it.isMappingTarget }

        if (primarySource == null) {
            logger.error("Mapper method must declare at least one source parameter. Method: ${method.simpleName.asString()}", method)
            return null
        }

        val returnType = method.returnType?.resolve()
        if (mappingTarget == null && returnType != null) {
            val returnTypeName = returnType.declaration.qualifiedName?.asString()
            if (returnTypeName == "kotlin.Unit" || returnTypeName == "java.lang.Void") {
                logger.error("Non update mapper method must return a target type. Method: ${method.simpleName.asString()}", method)
                return null
            }
        }

        val ownMappings = AnnotationParser.collectMappings(method, logger)

        val inheritFrom = method.annotations
            .firstOrNull { it.shortName.asString() == "InheritConfiguration" }
            ?.getArgument("name") as? String

        val methodNeedNullCheck = method.annotations
            .firstOrNull { it.shortName.asString() == "MappingConfig" }
            ?.getArgument("isNeedNullCheck") as? Boolean

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

    private fun buildParameterDescriptor(param: KSValueParameter, propertyResolver: PropertyResolver): ParameterDescriptor {
        val name = param.name?.asString() ?: "param"
        val isTarget = param.annotations.any { it.shortName.asString() == "MappingTarget" }
        val type = param.type.resolve()
        val typeDeclaration = propertyResolver.asClassDeclaration(type)
        return ParameterDescriptor(param, name, type, typeDeclaration, isTarget)
    }

    private fun KSFunctionDeclaration.hasAnnotation(name: String): Boolean {
        return annotations.any { it.shortName.asString() == name }
    }
}
