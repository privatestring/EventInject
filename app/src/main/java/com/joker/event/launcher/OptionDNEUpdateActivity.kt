package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.option.OptionPositionGroupBean
import launcher.Boom

class OptionDNEUpdateActivity : Activity() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var positionGroupBean: OptionPositionGroupBean? = null

    @Boom(index = 2)
    var quantity: String? = null

    @Boom(index = 3)
    var allowDneQuantity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OptionDNEUpdateActivityLauncher.bind(this)
    }
}
