package launcher.wb.mapper.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import launcher.wb.mapper.MapperMethodDescriptor
import launcher.wb.mapper.MappingSpec
import launcher.wb.mapper.ParameterDescriptor
import launcher.wb.mapper.PropertyResolver
import launcher.wb.mapper.ResolvedExpression

/**
 * 表达式解析器：负责将 @Mapping 的 source/expression/constant 解析为 Java 表达式
 */
class ExpressionResolver(
    private val propertyResolver: PropertyResolver,
    private val logger: KSPLogger
) {

    /**
     * 根据 MappingSpec 解析表达式
     */
    fun resolveExpression(method: MapperMethodDescriptor, spec: MappingSpec): ResolvedExpression? {
        val expression = when {
            !spec.expression.isNullOrBlank() -> {
                val expr = spec.expression.trim()
                val processedExpr = if (expr.startsWith("java(") && expr.endsWith(")")) {
                    expr.substring(5, expr.length - 1)
                } else {
                    expr
                }
                ResolvedExpression(processedExpr, null)
            }
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> resolveSourceExpression(method, spec.source)
            else -> resolveImplicitExpression(method, spec.target)
        }
        if (expression == null) {
            logger.error("Unable to resolve source for mapping target '${spec.target}'.", method.element)
        }
        return expression
    }

    /**
     * 隐式表达式解析（自动同名映射）
     */
    fun resolveImplicitExpression(
        method: MapperMethodDescriptor,
        property: String,
        sourceParam: ParameterDescriptor? = null
    ): ResolvedExpression? {
        val param = sourceParam ?: method.primarySource ?: return null
        return buildGetterChain(param, listOf(property), method, silent = true)
    }

    /**
     * 显式 source 路径解析
     */
    fun resolveSourceExpression(method: MapperMethodDescriptor, sourcePath: String): ResolvedExpression? {
        val parts = sourcePath.split('.').filter { it.isNotBlank() }
        if (parts.isEmpty()) return null

        val firstSegment = parts.first()
        val parameter = method.parameters.filter { !it.isMappingTarget }.firstOrNull { it.name == firstSegment }

        return if (parameter != null) {
            val propertyPath = parts.drop(1)
            if (propertyPath.isEmpty()) ResolvedExpression(parameter.name, parameter.type)
            else buildGetterChain(parameter, propertyPath, method)
        } else {
            buildGetterChain(method.primarySource ?: return null, parts, method)
        }
    }

    /**
     * 构建 getter 链式表达式（如 source.address.city → source.getAddress().getCity()）
     */
    private fun buildGetterChain(
        parameter: ParameterDescriptor,
        path: List<String>,
        method: MapperMethodDescriptor,
        silent: Boolean = false
    ): ResolvedExpression? {
        if (path.isEmpty()) return ResolvedExpression(parameter.name, parameter.type)

        var currentDeclaration: KSClassDeclaration? = parameter.typeDeclaration
        var currentType: KSType = parameter.type
        var expression = parameter.name

        path.forEach { segment ->
            if (currentDeclaration == null) {
                if (!silent) logger.error("Cannot find type element for '$segment' on ${parameter.name}.", method.element)
                return null
            }

            val getters = propertyResolver.readableProperties(currentDeclaration)
            val getter = getters[segment]
            if (getter != null) {
                expression += if (getter.isFieldAccess) ".${getter.getterName}" else ".${getter.getterName}()"
                currentType = getter.type
                currentDeclaration = propertyResolver.asClassDeclaration(currentType)
            } else {
                val field = propertyResolver.findField(currentDeclaration, segment)
                if (field != null) {
                    expression += ".${field.simpleName.asString()}"
                    currentType = field.type.resolve()
                    currentDeclaration = propertyResolver.asClassDeclaration(currentType)
                } else {
                    if (!silent) logger.error("Cannot find getter or field for '$segment'.", method.element)
                    return null
                }
            }
        }
        return ResolvedExpression(expression, currentType)
    }
}
