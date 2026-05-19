package com.joker.event.convert

import java.math.BigDecimal

/**
 * 模拟目标类：订单 Entity（业务层）
 */
class OrderEntity : OrderEntityParent() {
    var orderId: String = ""
    var symbol: String = ""
    var quantity: Int = 0
    var price: BigDecimal = BigDecimal.ZERO   // 源类无同名属性
    var action: String = ""
    var status: Int = 0                       // 源类无同名属性
    var createTime: Long = 0L
    var updateTime: Long = 0L                 // 将通过 ignoreTargets 忽略
}
