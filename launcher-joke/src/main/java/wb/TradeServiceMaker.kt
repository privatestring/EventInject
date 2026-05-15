@file:Suppress("PackageDirectoryMismatch")

package launcher

/**
 * 用于自动生成聚合所有 Trade 接口的服务接口
 *
 * 注解处理器会：
 * 1. 扫描 scanPackages 中指定的包，找出所有继承自 baseInterface 的接口
 * 2. 分析继承关系，找出顶层大接口（避免重复继承）
 * 3. 生成一个聚合接口，继承所有顶层大接口 + additionalInterfaces
 * 
 * 使用示例:
 * <pre>
 * {@code
 * @TradeServiceAggregator(
 *     baseInterface = ITradeInterface.class,
 *     scanPackages = {
 *         "com.webull.commonmodule.trade.service.trade",
 *     },
 *     additionalInterfaces = {IService.class},
 *     packageName = "com.webull.commonmodule.trade.service",
 *     className = "ITradeManagerService"
 * )
 * interface TradeManagerServiceMarker {}
 * }
 * </pre>
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class TradeServiceMaker(
    /**
     * 基础接口类型，用于判断接口是否需要被聚合
     * 只有继承此接口的接口才会被收集
     */
    val baseInterface: kotlin.reflect.KClass<*>,
    /**
     * 要扫描的包名列表
     * 注解处理器会在这些包中查找继承自 baseInterface 的接口
     */
    val scanPackages: Array<String> = [],
    /**
     * 额外需要继承的接口
     */
    val additionalInterfaces: Array<kotlin.reflect.KClass<*>> = [],
    /**
     * 生成接口的包名
     * 如果为空，则使用注解所在类的包名
     */
    val packageName: String = "",
    /**
     * 生成接口的类名
     * 如果为空，则使用注解所在类的类名 + "Generated"
     */
    val className: String = ""
)
