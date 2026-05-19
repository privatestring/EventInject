package wb.bean

/**
 * 标记属性为无条件赋值。
 *
 * 被此注解标记的字段在生成的 update 函数中不做任何检查，直接赋值：
 * `xxx = from.xxx`
 *
 * 适用于每次推送都需要覆盖的字段，无论新值是否为空/零。
 * 也可用于默认被 SKIP 的类型（如 Boolean），强制生成赋值代码。
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoUpdateAlways
