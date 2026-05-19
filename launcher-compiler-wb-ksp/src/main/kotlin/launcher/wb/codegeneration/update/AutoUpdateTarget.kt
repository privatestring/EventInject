package launcher.wb.codegeneration.update

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * 收集到的目标类信息
 */
data class AutoUpdateTarget(
    val classDecl: KSClassDeclaration,
    val functionName: String,
    val parentClassName: String?,
    /** 父类 @AutoUpdate 中指定的 functionName，为空时按默认规则拼接 */
    val parentFunctionName: String? = null,
    val packageName: String,
    val stringCheck: String,
    val stringCheckImport: String
)

/**
 * 字段信息
 */
data class PropertyInfo(
    val name: String,
    val type: FieldType,
    /** 属性级别自定义检查条件，为 null 时使用类级别默认值 */
    val customCheck: String? = null,
    /** 属性级别自定义 conditionImport，为 null 时不额外 import */
    val customCheckImport: String? = null,
    /** 无条件赋值（@AutoUpdateAlways） */
    val alwaysUpdate: Boolean = false,
    /** 被 @AutoUpdateIgnore 标记忽略 */
    val ignored: Boolean = false,
    /** 忽略原因描述（用于生成注释） */
    val ignoreReason: String? = null
) {
    /** 实际访问名 */
    val accessName: String get() = name
}

/**
 * 字段类型分类
 */
enum class FieldType {
    STRING,          // String? → valueIsNotEmpty()
    INT,             // Int → != 0
    LONG,            // Long → != 0L
    DOUBLE,          // Double → != 0.0
    FLOAT,           // Float → != 0.0f
    NULLABLE_OBJECT, // Any? → != null
    OBJECT,          // 非空对象 → 直接赋值
    SKIP             // 跳过不生成（Boolean、IntArray 等）
}
