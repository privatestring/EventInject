package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.broker.common.home.page.fragment.hkpl.bean.HKPLPeriodTypeItem
import java.util.Date
import launcher.Boom

class HKInstSimulatePLIncomeActivity : Activity() {
    @Boom(index = 1)
    var accountKey: String? = null

    @Boom(index = 2)
    var periodType: HKPLPeriodTypeItem = HKPLPeriodTypeItem()

    @Boom(index = 3)
    var selectedDate: String = ""

    @Boom(index = 4)
    var profitLoss: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HKInstSimulatePLIncomeActivityLauncher.bind(this)
    }
}
