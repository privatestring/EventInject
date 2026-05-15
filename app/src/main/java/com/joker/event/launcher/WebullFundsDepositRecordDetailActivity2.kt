package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.TradeOperationMessage
import launcher.Boom
import launcher.MakeResult

@MakeResult(includeStartForResult = true)
class WebullFundsDepositRecordDetailActivity2 : Activity() {
    @Boom(index = 0, key = "transfer_id")
    var mId: String? = null
    @Boom(index = 1, key = "sec_account_id")
    var mAccountInfo: AccountInfo? = null
    @Boom(index = 2, key = "deposit_msg")
    var mOperationMessage: TradeOperationMessage? = null
    @Boom(index = 3, isOptional = true)
    var isFirst: Boolean? = null
    @Boom(index = 4, isOptional = true)
    var isDebitCard: Boolean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebullFundsDepositRecordDetailActivity2Launcher.bind(this)
    }
}
