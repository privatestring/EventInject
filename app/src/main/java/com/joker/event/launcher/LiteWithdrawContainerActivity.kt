package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.lite.deposit.ui.withdraw.viewmodel.LiteWithdrawPage
import launcher.Boom
import launcher.ParentCls
@ParentCls
class LiteWithdrawContainerActivity : Activity() {
    @Boom(index = 0)
    var page: LiteWithdrawPage = LiteWithdrawPage()
    @Boom(index = 1)
    var accountInfo: AccountInfo = AccountInfo()
    @Boom(index = 111, isOptional = true, key = "is_rtp_transfer")
    var isRtpDeposit: Boolean = false
    @Boom(index = 112, isOptional = true, key = "CARD_ID")
    var cardId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LiteWithdrawContainerActivityLauncher.bind(this)
    }
}
