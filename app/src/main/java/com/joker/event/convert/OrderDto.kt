package com.joker.event.convert

/**
 * 模拟源类：订单 DTO（来自网络层）
 */
class OrderDto : OrderDtoParent() {
    var orderId: String = ""
    var symbol: String = ""
    var quantity: Int = 0
    var priceStr: String = ""
    var action: String = ""
    var createTime: Long = 0L
    var remark: String? = null
}
