package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "savingOrderDetail")
class SavingOrderDetailsFragment : Fragment() {

    @Boom(index = 0, desc = "账户信息")
    var accountKey: String = ""

    @Boom(index = 1, desc = "订单ID")
    var orderId: String = ""

    @Boom(index = 2, isOptional = true, desc = "订单方向")
    var side: String? = null

    @Boom(index = 3, isOptional = true, desc = "tickerId")
    var tickerId: String? = null

}