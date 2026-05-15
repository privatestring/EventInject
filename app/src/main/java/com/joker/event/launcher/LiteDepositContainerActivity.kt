package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.lite.deposit.ui.deposit.viewmodel.LiteDepositPage
import launcher.Boom
import launcher.ParentCls
@ParentCls
class LiteDepositContainerActivity : Activity() {
    @Boom(index = 0)
    var page: LiteDepositPage = LiteDepositPage()
    @Boom(index = 1, key = "account")
    var accountInfo: AccountInfo = AccountInfo()
    @Boom(index = 111, isOptional = true, key = "is_rtp_transfer")
    var isRtpTransfer: Boolean = false
    @Boom(index = 112, isOptional = true, key = "CARD_ID")
    var cardId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LiteDepositContainerActivityLauncher.bind(this)
    }
}
