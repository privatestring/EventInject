package launcher.wb.mapper

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * Mapper 功能的数据模型定义（KSP 版本）。
 * 对应原 KAPT 版本的 MapperModels.kt，使用 KSP 类型替代 javax.lang.model 类型。
 */

/**
 * Mapper 接口的完整描述符
 */
data class MapperDescriptor(
    val mapperElement: KSClassDeclaration,
    val packageName: String,
    val implementationName: String,
    val methods: List<MapperMethodDescriptor>,
    val needNullCheck: Boolean = false,
    val beforeMappingMethods: List<KSFunctionDeclaration> = emptyList(),
    val afterMappingMethods: List<KSFunctionDeclaration> = emptyList(),
    val ignoredMethods: List<KSFunctionDeclaration> = emptyList(),
    val isKotlinSource: Boolean = false
)

/**
 * 单个映射方法的描述符
 */
data class MapperMethodDescriptor(
    val element: KSFunctionDeclaration,
    val name: String,
    val returnType: KSType,
    val parameters: List<ParameterDescriptor>,
    val mappingTarget: ParameterDescriptor?,
    val primarySource: ParameterDescriptor?,
    val ownMappings: List<MappingSpec>,
    val inheritFrom: String?,
    val needNullCheck: Boolean? = null
) {
    var resolvedMappings: List<MappingSpec> = emptyList()
}

/**
 * 方法参数描述符
 */
data class ParameterDescriptor(
    val element: KSValueParameter,
    val name: String,
    val type: KSType,
    val typeDeclaration: KSClassDeclaration?,
    val isMappingTarget: Boolean
)

/**
 * 单条映射规则
 */
data class MappingSpec(
    val target: String,
    val source: String?,
    val constant: String?,
    val expression: String?,
    val ignore: Boolean
)

/**
 * 解析后的表达式（包含表达式字符串和类型信息）
 */
data class ResolvedExpression(
    val expression: String,
    val type: KSType?
)
