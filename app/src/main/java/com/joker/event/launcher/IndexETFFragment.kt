package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class IndexETFFragment : Fragment() {
    @Boom(index = 0, key = "title")
    var pageTitle: String = ""

    @Boom(index = 1, key = "source")
    var etfCardTabList: String? = null

    @Boom(index = 2, isOptional = true, key = "default_show_tab_index")
    var defaultIndex: String? = "0"

    @Boom(index = 3, isOptional = true, key = "regionId")
    var regionId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        IndexETFFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
