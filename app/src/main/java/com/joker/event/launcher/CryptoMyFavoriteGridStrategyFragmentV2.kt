package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class CryptoMyFavoriteGridStrategyFragmentV2 : Fragment() {
    @Boom(index = 0)
    var accountKey: String? = ""

    @Boom(index = 1)
    var tickerId: String? = ""

    @Boom(index = 2, key = "disSymbol")
    var disSymbol: String? = null

    @Boom(index = 3, key = "scene")
    var scene: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CryptoMyFavoriteGridStrategyFragmentV2Launcher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
