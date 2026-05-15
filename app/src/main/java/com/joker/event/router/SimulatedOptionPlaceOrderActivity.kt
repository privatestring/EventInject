package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "simulate_trade_option_place_order")
class SimulatedOptionPlaceOrderActivity : Activity() {

    @Boom(index = 0, key = "paper_id", desc = "模拟交易账户ID")
    var paperId: String? = null

    @Boom(index = 1, key = "ticker_id", desc = "正股ID")
    var stockId: String = ""

    @Boom(index = 2, key = "option_Strategy", desc = "期权策略")
    var strategy: String = ""

    @Boom(index = 3, key = "option_leg_list", desc = "期权腿列表")
    var optionLegsJsonStr: String = ""

    @Boom(index = 4, key = "order_action", isOptional = true, desc = "订单方向")
    var action: String? = null

    @Boom(index = 5, key = "order_quantity", isOptional = true, desc = "订单数量")
    var quantity: String? = null

    @Boom(index = 6, key = "order_is_modify", isOptional = true, desc = "是否改单")
    var isModify: String? = null

    @Boom(index = 7, key = "order_is_close_position", isOptional = true, desc = "是否平仓")
    var isClosePosition: String? = null

    @Boom(index = 8, key = "order_json_str", isOptional = true, desc = "改单订单对象")
    var modifyOrderStr: String? = null

    @Boom(index = 9, key = "close_position_json_str", isOptional = true, desc = "平仓持仓对象")
    var closePositionStr: String? = null

}