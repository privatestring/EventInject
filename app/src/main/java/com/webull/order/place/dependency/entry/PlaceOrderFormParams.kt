package com.webull.order.place.dependency.entry

import com.webull.library.repository.constant.AtQuantityType
import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import java.math.BigDecimal

/** Stub: 下单表单参数（从 AppDev3 同步字段） */
class PlaceOrderFormParams {
    var orderAction: OrderActionEnum? = null
    var quantityType: AtQuantityType? = null
    var quantity: BigDecimal? = null
    var timeInForce: TimeInForceEnum? = null
    var orderType: String? = null
    var lmtPriceAtLimitOrder: BigDecimal? = null
    var auxPriceAtStopOrder: BigDecimal? = null
    var auxPriceAtStopLimitOrder: BigDecimal? = null
    var lmtPriceAtStopLimitOrder: BigDecimal? = null
    var legOutId: String? = null
    var comboType: String? = null
    var isLegOut: Boolean? = null

}
