package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "kalshi_account_pl_share")
class KalshiAccountPLShareActivity : Activity() {

    @Boom(index = 0, key = "key_share_brokerid", desc = "分享账号的brokerId")
    var brokerId: String? = null

    @Boom(index = 1, key = "openPlValue", desc = "账号盈亏")
    var openPlValue: String? = null

    @Boom(index = 2, key = "openPlratio", desc = "账号盈亏率")
    var openPlratio: String? = null

    @Boom(index = 3, key = "dayPlvalue", desc = "当日盈亏")
    var dayPlValue: String? = null

    @Boom(index = 4, key = "dayPlratio", desc = "当日盈亏率")
    var dayPlRatio: String? = null

}