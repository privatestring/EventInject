package wb.bean

/**
 * 标记需要自动生成 update 扩展函数的 Bean 类。
 *
 * KSP 处理器会扫描标注此注解的类，根据字段类型自动生成赋值代码：
 * - String? → if (from.xxx.valueIsNotEmpty()) xxx = from.xxx
 * - Int (非0判断) → if (from.xxx != 0) xxx = from.xxx
 * - Long (非0判断) → if (from.xxx != 0L) xxx = from.xxx
 * - 可空对象 → if (from.xxx != null) xxx = from.xxx
 *
 * 生成的函数只包含纯字段赋值，特殊业务逻辑需在手写代码中处理。
 *
 * @param functionName 生成的扩展函数名，为空时默认 "update{ClassName}Fields"
 * @param parent 父类 Class，生成时会先调用父类的 updateFields 方法。默认 Any 表示无父类。
 * @param packageName 生成代码的包名，为空时默认 [DEFAULT_PACKAGE]
 * @param stringCheck String 类型字段的判断表达式。使用 `{field}` 作为字段占位符。
 *                    默认 [DEFAULT_STRING_CHECK]，表示不为 null 且不为空字符串。
 *                    示例："{field} != null" 只判断非空，"{field}.isNotBlank()" 判断非空白。
 * @param stringCheckImport stringCheck 中使用的扩展函数的 import 路径（全限定名）。
 *                          默认 [DEFAULT_STRING_CHECK_IMPORT]。
 *                          如果 stringCheck 使用标准库方法（如 isNotEmpty），设为空字符串即可。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoUpdate(
    val functionName: String = "",
    val stringCheck: String = DEFAULT_STRING_CHECK,
    val stringCheckImport: String = DEFAULT_STRING_CHECK_IMPORT
) {
    companion object {
        const val DEFAULT_STRING_CHECK = "{field}.valueIsNotEmpty()"
        const val DEFAULT_STRING_CHECK_IMPORT = "wb.bean.valueIsNotEmpty"
    }
}

/**
 * 判断字符串不为 null 且不为空字符串。
 * 生成代码中 String 类型字段默认使用此方法作为赋值条件。
 */
fun String?.valueIsNotEmpty(default: String = "--"): Boolean {
    return this != null && this != default && this.isNotEmpty()
}
