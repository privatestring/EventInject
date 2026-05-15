package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class MarketCommonChildFragment : Fragment() {
    @Boom(index = 0)
    var region: String = "6"

    @Boom(index = 1)
    var isShowActionBar: String = ""

    @Boom(index = 2)
    var title: String = ""

    @Boom(index = 3, isOptional = true)
    var isSupportSearch: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MarketCommonChildFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
