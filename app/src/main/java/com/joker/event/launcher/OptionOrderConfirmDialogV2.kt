package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.OptionLeg
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.request.OptionOrderRequest
import java.util.ArrayList
import launcher.Boom

class OptionOrderConfirmDialogV2 : DialogFragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var request: OptionOrderRequest? = null

    @Boom(index = 2)
    var openOrClose: String? = null

    @Boom(index = 3)
    var isSimplePlaceOptionMode: Boolean = false

    @Boom(index = 4)
    var dialogWidth: Int = 0

    @Boom(index = 5)
    var userOrderPrice: String? = null

    @Boom(index = 6)
    var isOptionDiscover: Boolean = false

    @Boom(index = 7, isOptional = true)
    var discoverStrategy: String? = null

    @Boom(index = 8)
    var isOptionRolling: Boolean = false

    @Boom(index = 9)
    var strategyKey: String? = null

    @Boom(index = 10, isOptional = true)
    var rollingLegs: ArrayList<OptionLeg>? = null

    @Boom(index = 11, isOptional = true)
    var isCustomOptionStrategy: Boolean = false

    @Boom(index = 12, isOptional = true)
    var estimateAmount: String? = null

    @Boom(index = 13, isOptional = true)
    var estimateTransactionFee: String? = null

    @Boom(index = 14, isOptional = true)
    var initialMargin: String? = null

    @Boom(index = 15, isOptional = true)
    var currency: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionOrderConfirmDialogV2Launcher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
