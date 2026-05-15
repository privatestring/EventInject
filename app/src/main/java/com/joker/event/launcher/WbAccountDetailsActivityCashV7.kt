package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class WbAccountDetailsActivityCashV7 : Activity() {
    @Boom(index = 1)
    var mAccountInfo: AccountInfo = AccountInfo()

    @Boom(index = 2)
    var mNetAccountValue: String = "--"

    @Boom(index = 3)
    var mDayPl: String = "--"

    @Boom(index = 4)
    var mDayPlRatio: String = "--"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WbAccountDetailsActivityCashV7Launcher.bind(this)
    }
}
