package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "recurring_investment_order_detail")
class RecurringInvestmentOrderDetailActivity : Activity() {

    @Boom(index = 0, desc = "账户accountKey")
    var accountKey: String = ""

    @Boom(index = 1, desc = "定投id")
    var mPlanId: String = ""

}