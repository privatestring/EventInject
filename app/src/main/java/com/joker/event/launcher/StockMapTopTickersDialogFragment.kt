package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.market.stock.map.StockBubbleData
import launcher.Boom

class StockMapTopTickersDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var bubbleData: StockBubbleData? = null

    @Boom(index = 1)
    var x: Float? = null

    @Boom(index = 2)
    var y: Float? = null

    @Boom(index = 3)
    var radius: Float? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        StockMapTopTickersDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
