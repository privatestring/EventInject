package launcher.mapper

import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

data class MapperDescriptor(
    val mapperElement: TypeElement,
    val packageName: String,
    val implementationName: String,
    val methods: List<MapperMethodDescriptor>,
    val needNullCheck: Boolean = false,     // 是否需要在字段赋值时进行空值检查
    val beforeMappingMethods: List<ExecutableElement> = emptyList(),  // @BeforeMapping 方法列表
    val afterMappingMethods: List<ExecutableElement> = emptyList(),     // @AfterMapping 方法列表
    val ignoredMethods: List<ExecutableElement> = emptyList(),  // @MappingIgnore 方法列表（用于 expression 中调用）
    val isKotlinSource: Boolean = false     // 是否来自 Kotlin 源文件
)

data class MapperMethodDescriptor(
    val element: ExecutableElement,
    val name: String,
    val returnType: TypeMirror,
    val parameters: List<ParameterDescriptor>,
    val mappingTarget: ParameterDescriptor?,
    val primarySource: ParameterDescriptor?,
    val ownMappings: List<MappingSpec>,
    val inheritFrom: String?,
    val needNullCheck: Boolean? = null      // 方法级配置，null 表示使用类级配置
) {
    var resolvedMappings: List<MappingSpec> = emptyList()
}

data class ParameterDescriptor(
    val element: VariableElement,
    val name: String,
    val type: TypeMirror,
    val typeElement: TypeElement?,
    val isMappingTarget: Boolean
)

data class MappingSpec(
    val target: String,
    val source: String?,
    val constant: String?,
    val expression: String?,  // Java 表达式，支持自定义业务逻辑
    val ignore: Boolean
)

data class ResolvedExpression(
    val expression: String,
    val type: TypeMirror?
)

