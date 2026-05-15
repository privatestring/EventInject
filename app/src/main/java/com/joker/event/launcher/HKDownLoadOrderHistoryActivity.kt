package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.broker.common.home.page.fragment.history.HistoryType
import com.webull.library.broker.webull.account.model.BaseGetCapitalDetailsModel
import com.webull.library.tradenetwork.bean.AccountInfo
import java.util.ArrayList
import java.util.HashMap
import launcher.Boom

class HKDownLoadOrderHistoryActivity : Activity() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var historyType: HistoryType = HistoryType.Funds

    @Boom(index = 2)
    var fundsConditions: ArrayList<BaseGetCapitalDetailsModel.Condition> = arrayListOf()

    @Boom(index = 3)
    var params: HashMap<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HKDownLoadOrderHistoryActivityLauncher.bind(this)
    }
}
