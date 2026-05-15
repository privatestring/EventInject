package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class AuWefolioOrderDetailActivity : Activity() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var wefolioOrderId: String? = null

    @Boom(index = 2, isOptional = true)
    var wefolioPageExist: Boolean = false

    @Boom(index = 3, isOptional = true)
    var isWefolioPage: Boolean = true

    @Boom(index = 4, isOptional = true)
    var isSmartBalanceOrder: Boolean = false

    @Boom(index = 5, isOptional = true)
    var isComboId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuWefolioOrderDetailActivityLauncher.bind(this)
    }
}
