package com.joker.event.convert

import wb.bean.AutoConvert
import wb.bean.AutoConvertLifecycle
import java.math.BigDecimal

/**
 * 订单 DTO → Entity 转换器。
 *
 * 同名同类型属性自动映射，不匹配的在 onEnd 中手动处理。
 */
@AutoConvert(ignoreTargets = ["updateTime"])
class OrderDtoConverter : AutoConvertLifecycle<OrderDto, OrderEntity> {

    override fun onStart(source: OrderDto, target: OrderEntity) {
        // 转换前：可做校验等
    }

    override fun onEnd(source: OrderDto, target: OrderEntity) {
        // 转换后：手动处理不匹配的属性
        target.price = source.priceStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }
}
