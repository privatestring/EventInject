package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class CryptoGridStrategyListFragment : Fragment() {
    @Boom(index = 0, key = "accountKey")
    var accountKey: String = ""

    @Boom(index = 1, key = "tickerId")
    var tickerId: String? = null

    @Boom(index = 2, key = "selectedStatus")
    var selectedStatus: String? = null

    @Boom(index = 3, key = "disSymbol")
    var disSymbol: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CryptoGridStrategyListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
