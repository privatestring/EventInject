package wb.bean

/**
 * 标记一个转换类，KSP 将自动生成源类到目标类的扩展函数。
 *
 * 使用方式：
 * ```kotlin
 * @AutoConvert
 * class OrderDtoConverter : ConvertLifecycle<OrderDto, OrderEntity> {
 *     override fun onEnd(source: OrderDto, target: OrderEntity) {
 *         // 手动处理不匹配的属性
 *         target.price = source.priceStr.toBigDecimal()
 *     }
 * }
 * ```
 *
 * 生成代码：
 * ```kotlin
 * fun OrderDto.convertToOrderEntity(converter: OrderDtoConverter = OrderDtoConverter()): OrderEntity {
 *     val target = OrderEntity()
 *     converter.onStart(this, target)
 *     target.orderId = this.orderId
 *     target.symbol = this.symbol
 *     converter.onEnd(this, target)
 *     return target
 * }
 * ```
 *
 * 规则：
 * - 源类和目标类从 ConvertLifecycle<S, T> 的泛型参数中推断
 * - 同名且类型兼容的属性自动赋值
 * - 目标类中未匹配的属性会在生成文件末尾以注释形式列出
 * - 目标类必须有无参构造函数
 *
 * @param ignoreTargets 需要忽略的目标类属性名列表，这些属性不参与自动映射也不会出现在未匹配注释中
 * @param functionName 生成的扩展函数名，为空时默认 "convertTo{TargetClassName}"
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoConvert(
    val ignoreTargets: Array<String> = [],
    val functionName: String = ""
)
