package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class ETFScreenerFragment : Fragment() {
    @Boom(index = 0, isOptional = true, key = "regionId")
    var regionId: String = ""

    @Boom(index = 1, key = "rankType")
    var rankType: String? = null

    @Boom(index = 2, isOptional = true, key = "isHot")
    var isHot: String? = null

    @Boom(index = 3, isOptional = true, key = "title")
    var pageTitle: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ETFScreenerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
