package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.tradeapi.beans.SimpleTickerInfo.QuantityLevel
import java.util.ArrayList
import launcher.Boom

class SelectQuantityDialog : DialogFragment() {
    @Boom(index = 1)
    var mList: ArrayList<QuantityLevel>? = null

    @Boom(index = 2)
    var currencyId: Int = 0

    @Boom(index = 3)
    var currentQuantity: String? = null

    @Boom(index = 4)
    var maxCanBuyQuantity: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SelectQuantityDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
