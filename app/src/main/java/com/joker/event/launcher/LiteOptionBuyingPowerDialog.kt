package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class LiteOptionBuyingPowerDialog : DialogFragment() {
    @Boom(index = 0)
    var bpUsed: String? = null

    @Boom(index = 1)
    var optionBp: String? = null

    @Boom(index = 2)
    var dayTradeLeft: String? = null

    @Boom(index = 3)
    var stockId: String? = null

    @Boom(index = 4)
    var optionTickerId: String? = null

    @Boom(index = 5)
    var isClose: Boolean? = null

    @Boom(index = 6)
    var isModify: Boolean? = null

    @Boom(index = 7)
    var isDiscover: Boolean? = null

    @Boom(index = 8)
    var isLegIn: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteOptionBuyingPowerDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
