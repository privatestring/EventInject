package wb.bean

/**
 * 标记不参与自动赋值的字段。
 *
 * 被此注解标记的字段在 KSP 生成 update 扩展函数时会被跳过，
 * 需要在手写代码中单独处理。
 *
 * 典型场景：
 * - 有特殊赋值逻辑的字段（如 status 需要判断夜盘状态）
 * - 需要条件性赋值的字段（如 secType 需要判断 isHttpRequest）
 * - 本地标记字段（如 isPush、isHistory）
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoUpdateIgnore
