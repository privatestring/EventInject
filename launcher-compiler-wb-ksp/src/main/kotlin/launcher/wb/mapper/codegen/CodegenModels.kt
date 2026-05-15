package launcher.wb.mapper.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import launcher.wb.mapper.ResolvedExpression

/**
 * 代码生成内部数据模型
 */

/** 目标对象上下文 */
data class TargetContext(
    val type: KSType,
    val typeDeclaration: KSClassDeclaration,
    val varName: String,
    val isUpdate: Boolean
)

/** 赋值操作密封类 */
sealed class Assignment {
    abstract val expression: String
    abstract val expressionType: KSType?
}

/** 通过 setter 方法赋值 */
data class PropertyAssignment(
    val setterName: String,
    override val expression: String,
    override val expressionType: KSType?
) : Assignment()

/** 通过字段直接赋值 */
data class FieldAssignment(
    val fieldName: String,
    override val expression: String,
    override val expressionType: KSType?
) : Assignment()

/** 嵌套对象赋值 */
data class NestedAssignment(
    val rootProperty: String,
    val rootSetterName: String?,
    val rootFieldName: String?,
    val nestedPath: List<String>,
    val sourceExpression: ResolvedExpression,
    val intermediateType: KSType,
    val intermediateDeclaration: KSClassDeclaration
) : Assignment() {
    override val expression: String = sourceExpression.expression
    override val expressionType: KSType? = sourceExpression.type
}
