package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.commonmodule.networkinterface.quoteapi.beans.wefolio.WefolioTickerListData
import launcher.Boom

class WefolioContainerActivity : Activity() {
    @Boom(index = 0, key = "pageType")
    var pageType: String = ""

    @Boom(index = 1, key = "current_wefolio_data")
    var currentWefolioData: WefolioTickerListData? = null

    @Boom(index = 2, isOptional = true, key = "wefolio_param_data")
    var wefolioParamData: String? = null

    @Boom(index = 3, isOptional = true)
    var wefolioTickerListDataItemJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WefolioContainerActivityLauncher.bind(this)
    }
}
