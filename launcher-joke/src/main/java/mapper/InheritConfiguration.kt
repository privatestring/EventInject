package mapper

/**
 * 复用另一映射方法配置的简化注解。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class InheritConfiguration(
    /**
     * 需要继承配置的方法名。
     */
    val name: String
)
