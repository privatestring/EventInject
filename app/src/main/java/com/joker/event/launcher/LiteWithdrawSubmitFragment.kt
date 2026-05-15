package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.AchAcct
import launcher.Boom

class LiteWithdrawSubmitFragment : Fragment() {
    @Boom(index = 0, key = "account")
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 1, key = "ach")
    var bankCardInfo: AchAcct = AchAcct()

    @Boom(index = 110, isOptional = true, key = "amount")
    var initAmount: String = ""

    @Boom(index = 111, isOptional = true, key = "is_rtp_transfer")
    var isRtpTransfer: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteWithdrawSubmitFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
