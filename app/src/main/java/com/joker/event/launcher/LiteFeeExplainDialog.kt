package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.trade.bean.OrderFeeDetails
import launcher.Boom

class LiteFeeExplainDialog : DialogFragment() {
    @Boom(index = 0)
    var brokerId: Int = 0

    @Boom(index = 1)
    var currency: Int = 0

    @Boom(index = 2)
    var orderFeeDetails: OrderFeeDetails? = null

    @Boom(index = 3)
    var descStr: String? = null

    @Boom(index = 4, isOptional = true)
    var titleStr: String? = null

    @Boom(index = 5)
    var receivableFee: String? = null

    @Boom(index = 6)
    var estimateAmount: String? = null

    @Boom(index = 7)
    var type: Int? = null

    @Boom(index = 8)
    var action: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteFeeExplainDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
