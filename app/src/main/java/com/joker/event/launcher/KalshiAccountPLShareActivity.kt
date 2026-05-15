package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class KalshiAccountPLShareActivity : Activity() {
    @Boom(index = 0, key = "key_share_brokerid")
    var brokerId: String? = null

    @Boom(index = 1, key = "openPlValue")
    var openPlValue: String ? = null

    @Boom(index = 2, key = "openPlratio")
    var openPlratio: String ? = null

    @Boom(index = 3, key = "dayPlvalue")
    var dayPlValue: String ? = null

    @Boom(index = 4, key = "dayPlratio")
    var dayPlRatio: String ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KalshiAccountPLShareActivityLauncher.bind(this)
    }
}
