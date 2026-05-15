package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.trade.funds.webull.deposit.ira.confirm.IraDepositRequest
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.robot.advisor.request.AdvisorAvailableFundsResponse
import launcher.Boom

class LiteWebullDepositSubmitDialog : DialogFragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 1)
    var webullInfo: AccountInfo = AccountInfo()

    @Boom(index = 2)
    var inputAmount: String = ""

    @Boom(index = 3)
    var availableFundsInfo: AdvisorAvailableFundsResponse = AdvisorAvailableFundsResponse()

    @Boom(index = 4, isOptional = true)
    var iraDepositRequest: IraDepositRequest? = null

    @Boom(index = 5, isOptional = true)
    var matchAmount: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteWebullDepositSubmitDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
