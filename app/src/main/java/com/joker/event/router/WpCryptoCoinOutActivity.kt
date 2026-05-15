package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "wpCryptoCoinOut")
class WpCryptoCoinOutActivity : Activity() {

    @Boom(index = 0, key = "accountKey", useFieldKey = true, desc = "账户Key")
    var accountKey: String? = null

    @Boom(index = 1, key = "tickerId", useFieldKey = true, desc = "币种ID")
    var tickerId: String? = null

}