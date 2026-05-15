package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.core.framework.bean.TickerAskBid
import com.webull.library.broker.webull.option.OptionFieldsObj
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.order.OptionOrderGroupBean
import com.webull.library.tradenetwork.bean.request.OptionComboOrderRequest
import java.util.ArrayList
import launcher.Boom

class OptionComboOrderConfirmDialog : DialogFragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var request: OptionComboOrderRequest? = null

    @Boom(index = 2, isOptional = true)
    var fieldsObjV2: OptionFieldsObj? = null

    @Boom(index = 3, isOptional = true)
    var lmtProfitOrder: OptionOrderGroupBean? = null

    @Boom(index = 4, isOptional = true)
    var stopLossOrder: OptionOrderGroupBean? = null

    @Boom(index = 5, isOptional = true)
    var isClosePosition: Boolean = false

    @Boom(index = 6, isOptional = true)
    var costPrice: String? = null

    @Boom(index = 7, isOptional = true)
    var mBidList: ArrayList<TickerAskBid>? = null

    @Boom(index = 8, isOptional = true)
    var mAskList: ArrayList<TickerAskBid>? = null

    @Boom(index = 9, isOptional = true)
    var positionCount: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionComboOrderConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
