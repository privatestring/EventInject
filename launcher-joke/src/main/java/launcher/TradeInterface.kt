package launcher

/**
 * 用于标记Trade接口的实现类
 * 注解处理器会在编译时收集所有标记此注解的类，并自动生成TradeInterfaceFactory
 *
 * 使用示例:
 * @TradeInterface(ITradeAccountInterface::class)
 * class TradeAccountInterfaceImpl : ITradeAccountInterface { ... }
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class TradeInterface(
    /**
     * 接口类型，实现类需要实现此接口
     */
    val value: kotlin.reflect.KClass<*>,
    /**
     * 是否为内部接口
     */
    val isInner: Boolean = false
)
