package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.lite.deposit.request.response.AssetsTransferVO
import com.webull.robot.advisor.request.AdvisorCashInnerTransferResponse
import launcher.Boom

class LiteWebullDepositSuccessFragment : Fragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 1, isOptional = true)
    var advisorResult: AdvisorCashInnerTransferResponse? = null

    @Boom(index = 2, isOptional = true)
    var wbResult: AssetsTransferVO? = null

    @Boom(index = 99, isOptional = true)
    var fromType: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteWebullDepositSuccessFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
