package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.core.framework.bean.TickerBase
import launcher.Boom

class UsFundPlaceOrderFragment : Fragment() {
    @Boom(index = 0)
    var brokerId: Int = 0

    @Boom(index = 1)
    var tickerBase: TickerBase? = null

    @Boom(index = 2, isOptional = true)
    var isBuy: Boolean = true

    @Boom(index = 3, isOptional = true)
    var isModify: Boolean = true

    @Boom(index = 4, isOptional = true)
    var modifyOrderId: String? = null

    @Boom(index = 5, isOptional = true)
    var modifyQuantity: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UsFundPlaceOrderFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
