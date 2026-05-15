package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.AchAcct
import com.webull.library.tradenetwork.bean.AchResult
import launcher.Boom

class LiteDepositSuccessFragment : Fragment() {
    @Boom(index = 0)
    var result: AchResult = AchResult()

    @Boom(index = 1)
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 2)
    var bankCardInfo: AchAcct? = null

    @Boom(index = 3, isOptional = true, key = "is_rtp_transfer")
    var isRtpTransfer: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteDepositSuccessFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
