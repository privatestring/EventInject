package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.tradeapi.beans.HKIPOOrderDetail
import launcher.Boom

class StockIPOOrderConfirmDialog : DialogFragment() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var tickerId: String = ""

    @Boom(index = 3)
    var mOrderInfo: HKIPOOrderDetail? = null

    @Boom(index = 4, isOptional = true)
    var needShowBackIcon: Boolean? = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        StockIPOOrderConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
