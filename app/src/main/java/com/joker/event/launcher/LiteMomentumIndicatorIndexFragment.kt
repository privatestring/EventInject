package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class LiteMomentumIndicatorIndexFragment : Fragment() {
    @Boom(index = 0, key = "tab")
    var ranking: String = ""

    @Boom(index = 1)
    var rankingType: String = ""

    @Boom(index = 2)
    var tickerType: String? = null

    @Boom(index = 3)
    var close: String? = null

    @Boom(index = 4)
    var rankingOrder: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteMomentumIndicatorIndexFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
