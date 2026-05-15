package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class AiTickerVegaAnalystFragment : Fragment() {
    @Boom(index = 0, key = "key_ticker_id")
    var tickerId: String = ""

    @Boom(index = 1, key = "key_ticker_dis_symbol")
    var symbol: String = ""

    @Boom(index = 3, isOptional = true, key = "custom_text")
    var customText: String? = null

    @Boom(index = 2, isOptional = true, key = "source")
    var source: String? = null

    @Boom(index = 4, isOptional = true, key = "history_config")
    var configHistory: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AiTickerVegaAnalystFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
