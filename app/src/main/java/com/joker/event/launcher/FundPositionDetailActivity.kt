package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class FundPositionDetailActivity : Activity() {
    @Boom(index = 0)
    var tickerId: String? = null

    @Boom(index = 1)
    var mAccountInfo: AccountInfo? = null

    @Boom(index = 2)
    var template: String? = null

    @Boom(index = 3)
    var regionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FundPositionDetailActivityLauncher.bind(this)
    }
}
