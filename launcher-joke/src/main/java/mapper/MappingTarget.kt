package mapper

/**
 * 标记需要被更新的目标参数
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class MappingTarget
