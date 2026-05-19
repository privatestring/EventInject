package wb.bean

/**
 * 标记属性使用自定义的检查方式。
 *
 * 适用于所有字段类型（String、Int、Long、可空对象等）。
 * 未标注此注解的属性使用默认检查逻辑：
 * - String → 类级别 @AutoUpdate.stringCheck
 * - Int → != 0
 * - Long → != 0L
 * - 可空对象 → != null
 *
 * 标注此注解后，该属性将使用此处指定的表达式作为赋值条件。
 *
 * @param condition 该字段的判断表达式。支持两个占位符：
 *                  - `{field}` → 替换为 `from.属性名`（如 `from.volume`）
 *                  - `{from}` → 替换为源对象 `from`（用于访问任意 getter/属性）
 *                  示例：
 *                  - String: "{field}.isNotBlank()"
 *                  - Int: "{field} > 0"
 *                  - Long: "{field} != -1L"
 *                  - 可空对象: "{field} != null && {field}.tickerId.isNotEmpty()"
 *                  - 跨 getter: "{from}.listStatusInteger != null"
 * @param conditionImport 表达式中使用的扩展函数的 import 路径（全限定名）。
 *                        如果使用标准库方法，设为空字符串即可。
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoUpdateCheck(
    val condition: String,
    val conditionImport: String = ""
)
