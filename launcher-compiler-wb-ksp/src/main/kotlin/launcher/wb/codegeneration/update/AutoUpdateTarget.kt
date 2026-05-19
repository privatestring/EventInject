package launcher.wb.codegeneration.update

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * 收集到的目标类信息
 */
data class AutoUpdateTarget(
    val classDecl: KSClassDeclaration,
    val functionName: String,
    val parentClassName: String?,
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
    val jvmName: String? = null
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
    NULLABLE_OBJECT, // Any? → != null
    SKIP             // 跳过不生成
}
