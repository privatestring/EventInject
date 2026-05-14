package mapper

/**
 * 该注解，用于自动生成实体类更新/转换代码，使用时需添加到描述接口类上
 * 适合使用的场景：
 * 1、纯字段转换，不含任务业务逻辑
 * 2、转换时，包含部分简单业务逻辑，比如日期/金额格式化、或是根据其它字段情况，取不同值这种
 * 不适合使用的场景：
 * 1、实体类自身，包含不合理的使用。比如get和set方法，关联的属性不匹配，会导致赋值错乱
 * （e.g. TickerRealTime）
 * 2、业务逻辑较重的转换代码，条件判断较多，且包含复杂业务
 * 3、原实体类转换代码，使用场景有限，非通用场景。只是在一些特殊场景下，仅需要个别字段的转换，而非全部转换
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Mapper(
    /**
     * 预留兼容属性，当前实现仅做占位。
     */
    val uses: Array<kotlin.reflect.KClass<*>> = [],
    /**
     * 组件模型名称，当前实现不做特殊处理，保留语义兼容。
     */
    val componentModel: String = "",
    /**
     * 生成实现类使用的后缀，默认 Impl。
     */
    val implementationSuffix: String = "Impl"
)
