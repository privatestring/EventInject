package mapper

/**
 * Mapper 配置注解
 * 用于控制 Mapper 接口或方法的映射行为
 * 可以标注在类上（全局配置）或方法上（方法级配置）
 * 方法级配置会覆盖类级配置
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class MappingConfig(
    /**
     * 是否需要在字段赋值时进行空值检查
     * true: 在赋值前检查源值是否为 null，只有非 null 时才赋值
     * false: 直接赋值，不进行空值检查（默认行为）
     */
    val isNeedNullCheck: Boolean = true
)
