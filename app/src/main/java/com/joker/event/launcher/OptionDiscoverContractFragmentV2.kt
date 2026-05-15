package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class OptionDiscoverContractFragmentV2 : Fragment() {
    @Boom(index = 0)
    var tickerId: String? = null

    @Boom(index = 1)
    var unSymbol: String? = null

    @Boom(index = 2, isOptional = true)
    var pagerId: String = ""

    @Boom(index = 3, isOptional = true)
    var isFromLiteMore: Boolean? = null

    @Boom(index = 4, isOptional = true)
    var isFromLiteStayBelow: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionDiscoverContractFragmentV2Launcher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
