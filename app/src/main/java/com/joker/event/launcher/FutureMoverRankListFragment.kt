package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class FutureMoverRankListFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String = ""

    @Boom(index = 1, key = "groupId")
    var groupId: String = ""

    @Boom(index = 2, isOptional = true, key = "tabId")
    var currentTabId: String? = null

    @Boom(index = 3, isOptional = true, key = "tabData")
    var tabDataStr: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FutureMoverRankListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
