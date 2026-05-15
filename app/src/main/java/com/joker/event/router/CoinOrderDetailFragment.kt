package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "cryptoCoinOrderDetail")
class CoinOrderDetailFragment : Fragment() {

    @Boom(index = 0, key = "orderId", desc = "转仓订单id")
    var orderId: String = ""

    @Boom(index = 1, key = "accountKey", desc = "账户accountKey")
    var accountKey: String = ""

}