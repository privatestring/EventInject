package mapper

/**
 * [Mapping] 的容器注解，兼容早期 Java 版本不支持可重复注解的场景。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class Mappings(
    vararg val value: Mapping
)
