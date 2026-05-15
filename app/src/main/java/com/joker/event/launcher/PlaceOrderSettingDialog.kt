package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.core.framework.bean.TickerBase
import launcher.Boom

class PlaceOrderSettingDialog : DialogFragment() {
    @Boom(index = 0)
    var mBrokerId: Int = 0

    @Boom(index = 1)
    var mTicker: TickerBase? = null

    @Boom(index = 2)
    var canSwitchMode: Boolean = false

    @Boom(index = 4)
    var isSupportDayTradeMode: Boolean = false

    @Boom(index = 5, isOptional = true)
    var mBrokerAccountId: String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PlaceOrderSettingDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
