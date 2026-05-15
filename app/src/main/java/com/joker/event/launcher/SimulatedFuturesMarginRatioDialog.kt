package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class SimulatedFuturesMarginRatioDialog : DialogFragment() {
    @Boom(index = 0)
    var imr: String? = null

    @Boom(index = 1)
    var mmr: String? = null

    @Boom(index = 2)
    var buyingPower: String? = null

    @Boom(index = 3)
    var marginRatio: String? = null

    @Boom(index = 4)
    var currency: String? = null

    @Boom(index = 5)
    var intradayMargin: String? = null

    @Boom(index = 6)
    var intradayBuyingPower: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SimulatedFuturesMarginRatioDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
