package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.trade.order.common.FieldsObjV2
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.PlaceOrder
import launcher.Boom

class StopLossConfirmDialog : DialogFragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var fieldsObjV2: FieldsObjV2? = null

    @Boom(index = 2)
    var lmtProfitOrder: PlaceOrder? = null

    @Boom(index = 3)
    var stopLossOrder: PlaceOrder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        StopLossConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
