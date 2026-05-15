package launcher.wb.mapper.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import launcher.wb.mapper.MapperDescriptor
import launcher.wb.mapper.MapperMethodDescriptor
import launcher.wb.mapper.MappingSpec
import launcher.wb.mapper.ParameterDescriptor
import launcher.wb.mapper.PropertyResolver
import launcher.wb.mapper.ResolvedExpression

/**
 * 赋值收集器：负责收集显式映射、自动映射、嵌套映射的赋值操作
 */
class AssignmentCollector(
    private val descriptor: MapperDescriptor,
    private val propertyResolver: PropertyResolver,
    private val logger: KSPLogger
) {

    private val expressionResolver = ExpressionResolver(propertyResolver, logger)

    /**
     * 收集所有字段赋值
     */
    fun collectAssignments(method: MapperMethodDescriptor, targetContext: TargetContext): List<Assignment> {
        val assignments = mutableListOf<Assignment>()
        val setterMap = propertyResolver.writeableProperties(targetContext.typeDeclaration)
        val fieldMap = propertyResolver.writableFields(targetContext.typeDeclaration)
        val handledTargets = mutableSetOf<String>()

        // 显式映射
        method.resolvedMappings.forEach { spec ->
            handledTargets += spec.target.split('.').first()
            if (spec.ignore) return@forEach

            val targetPath = spec.target.split('.').filter { it.isNotBlank() }
            if (targetPath.size > 1) {
                createNestedAssignment(method, targetContext, spec, targetPath)?.let { assignments += it }
            } else {
                val setter = setterMap[spec.target]
                val field = fieldMap[spec.target]
                when {
                    setter != null -> createSpecAssignment(method, spec, setter.paramType) { expr, type ->
                        PropertyAssignment(setter.setterName, expr, type)
                    }?.let { assignments += it }
                    field != null -> createSpecAssignment(method, spec, field.type) { expr, type ->
                        FieldAssignment(field.name, expr, type)
                    }?.let { assignments += it }
                    else -> logger.error("No setter or writable field found for target '${spec.target}'.", method.element)
                }
            }
        }

        // 自动映射
        val allWritableTargets = (setterMap.keys + fieldMap.keys).toSet()
        val autoTargets = allWritableTargets - handledTargets
        val primarySource = method.primarySource

        if (autoTargets.isNotEmpty() && primarySource != null) {
            autoTargets.forEach { property ->
                val setter = setterMap[property]
                val field = fieldMap[property]
                when {
                    setter != null -> {
                        val assignment = createAutoAssignment(method, primarySource, property, setter.paramType) { expr, type ->
                            PropertyAssignment(setter.setterName, expr, type)
                        }
                        if (assignment != null) assignments += assignment
                        else checkTypeMismatchSilent(primarySource, property, setter.paramType)
                    }
                    field != null -> {
                        val assignment = createAutoAssignment(method, primarySource, property, field.type) { expr, type ->
                            FieldAssignment(field.name, expr, type)
                        }
                        if (assignment != null) assignments += assignment
                        else checkTypeMismatchSilent(primarySource, property, field.type)
                    }
                }
            }
        }

        return assignments
    }

    // ======================== 统一赋值创建 ========================

    /**
     * 从 MappingSpec 创建赋值（统一 Property/Field 逻辑）
     */
    private fun <T : Assignment> createSpecAssignment(
        method: MapperMethodDescriptor,
        spec: MappingSpec,
        targetType: KSType,
        factory: (String, KSType?) -> T
    ): T? {
        val expression = expressionResolver.resolveExpression(method, spec) ?: return null

        // expression 直接使用，不做类型检查
        if (!spec.expression.isNullOrBlank()) {
            return factory(expression.expression, null)
        }

        return resolveAssignment(expression, targetType, method, spec.target, factory, reportError = true)
    }

    /**
     * 自动映射创建赋值（统一 Property/Field 逻辑）
     */
    private fun <T : Assignment> createAutoAssignment(
        method: MapperMethodDescriptor,
        sourceParam: ParameterDescriptor,
        property: String,
        targetType: KSType,
        factory: (String, KSType?) -> T
    ): T? {
        val expression = expressionResolver.resolveImplicitExpression(method, property, sourceParam) ?: return null
        return resolveAssignment(expression, targetType, method, property, factory, reportError = false)
    }

    /**
     * 核心：解析表达式类型兼容性并创建赋值
     */
    private fun <T : Assignment> resolveAssignment(
        expression: ResolvedExpression,
        targetType: KSType,
        method: MapperMethodDescriptor,
        targetName: String,
        factory: (String, KSType?) -> T,
        reportError: Boolean
    ): T? {
        if (!TypeResolver.isAssignable(expression.type, targetType)) {
            val resolved = tryResolveTypeMismatch(expression, targetType)
            if (resolved != null) return factory(resolved.first, resolved.second)
            if (reportError) {
                logger.error("Type mismatch for property '$targetName'. Source: ${expression.type}, Target: $targetType", method.element)
            }
            return null
        }

        // 集合类型转换（类型兼容但具体类型不同，如 List → ArrayList）
        val convExpr = tryCollectionConversion(expression, targetType)
        if (convExpr != null) return factory(convExpr, targetType)

        return factory(expression.expression, expression.type)
    }

    // ======================== 嵌套映射 ========================

    private fun createNestedAssignment(
        method: MapperMethodDescriptor,
        targetContext: TargetContext,
        spec: MappingSpec,
        targetPath: List<String>
    ): NestedAssignment? {
        val rootProperty = targetPath.first()
        val nestedPath = targetPath.drop(1)

        val setterMap = propertyResolver.writeableProperties(targetContext.typeDeclaration)
        val fieldMap = propertyResolver.writableFields(targetContext.typeDeclaration)
        val rootSetter = setterMap[rootProperty]
        val rootField = fieldMap[rootProperty]

        if (rootSetter == null && rootField == null) {
            logger.error("Cannot find root property '$rootProperty' for nested target '${spec.target}'.", method.element)
            return null
        }

        val sourceExpression = when {
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> expressionResolver.resolveSourceExpression(method, spec.source)
            else -> expressionResolver.resolveImplicitExpression(method, nestedPath.joinToString("."))
        }

        if (sourceExpression == null) {
            logger.error("Unable to resolve source for nested target '${spec.target}'.", method.element)
            return null
        }

        val intermediateType = rootSetter?.paramType ?: rootField?.type ?: return null
        val intermediateDeclaration = propertyResolver.asClassDeclaration(intermediateType)
        if (intermediateDeclaration == null) {
            logger.error("Cannot resolve intermediate type for nested target '${spec.target}'.", method.element)
            return null
        }

        return NestedAssignment(rootProperty, rootSetter?.setterName, rootField?.name, nestedPath, sourceExpression, intermediateType, intermediateDeclaration)
    }

    // ======================== 类型不匹配处理 ========================

    private fun tryResolveTypeMismatch(expression: ResolvedExpression, targetType: KSType): Pair<String, KSType?>? {
        // 集合元素类型不匹配 → 查找元素映射方法
        if (TypeResolver.isCollectionType(expression.type) && TypeResolver.isCollectionType(targetType)) {
            val sourceElementType = TypeResolver.getCollectionElementType(expression.type)
            val targetElementType = TypeResolver.getCollectionElementType(targetType)
            if (sourceElementType != null && targetElementType != null) {
                if (!TypeResolver.isAssignable(sourceElementType, targetElementType)) {
                    val mapperMethod = TypeResolver.findElementMapperMethod(sourceElementType, targetElementType, descriptor.methods)
                    if (mapperMethod != null) {
                        return "COLLECTION_MAPPING:${expression.expression}:${mapperMethod.name}" to targetType
                    }
                } else {
                    val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
                    if (convExpr != null) return convExpr to targetType
                }
            }
        }

        // 普通类型 → 查找直接映射方法
        val directMethod = TypeResolver.findElementMapperMethod(expression.type, targetType, descriptor.methods)
        if (directMethod != null) {
            val expr = "${expression.expression} == null ? null : ${descriptor.implementationName}.${directMethod.name}(${expression.expression})"
            return expr to targetType
        }

        return null
    }

    private fun tryCollectionConversion(expression: ResolvedExpression, targetType: KSType): String? {
        if (TypeResolver.isCollectionType(expression.type) && TypeResolver.isCollectionType(targetType)) {
            return generateCollectionConversionExpression(expression.expression, expression.type, targetType)
        }
        return null
    }

    private fun generateCollectionConversionExpression(sourceExpr: String, sourceType: KSType?, targetType: KSType?): String? {
        if (sourceType == null || targetType == null) return null
        if (TypeResolver.isSameType(sourceType, targetType)) return null
        val targetTypeName = TypeResolver.getCollectionTypeName(targetType) ?: return null
        return "$sourceExpr == null ? null : new $targetTypeName<>($sourceExpr)"
    }

    /**
     * 类型不匹配时静默检查（与 KAPT 行为一致，不报错）
     */
    private fun checkTypeMismatchSilent(primarySource: ParameterDescriptor, property: String, targetType: KSType) {
        // 静默跳过：source 有该属性但类型不兼容且无映射方法
    }
}
