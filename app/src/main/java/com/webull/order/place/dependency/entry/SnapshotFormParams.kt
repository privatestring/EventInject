package com.webull.order.place.dependency.entry

import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import java.math.BigDecimal

/** Stub: 快照表单参数（从 AppDev3 同步字段） */
class SnapshotFormParams {
    var orderAction: OrderActionEnum? = null
    var quantity: BigDecimal? = null
    var timeInForce: TimeInForceEnum? = null
    var lmtPriceAtLimitOrder: BigDecimal? = null
    var auxPriceAtStopOrder: BigDecimal? = null
    var trailingStopStep: String? = null
    var trailingType: String? = null
    var orderType: String? = null
    var comboType: String? = null
}
