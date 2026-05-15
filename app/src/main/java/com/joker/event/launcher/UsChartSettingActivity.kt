package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.subchart.style.manager.IndicatorManagerViewModel
import launcher.Boom

class UsChartSettingActivity : Activity() {
    @Boom(index = 0, key = "key_is_mini_chart")
    var isMiniChart: String = ""

    @Boom(index = 1, key = "key_region_id")
    var regionId: String = ""

    @Boom(index = 2, key = "key_exchange_code")
    var exchangeCode: String = ""

    @Boom(index = 3, key = "key_cry_pto")
    var isCrypto: String = ""

    @Boom(index = 4, key = "key_newchart_indictor_manager")
    var indicatorManagerVMClass: Class<out IndicatorManagerViewModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UsChartSettingActivityLauncher.bind(this)
    }
}
