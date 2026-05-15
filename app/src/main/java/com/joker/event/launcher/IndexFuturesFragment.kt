package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class IndexFuturesFragment : Fragment() {
    @Boom(index = 0, key = "region_id")
    var region: String = "6"

    @Boom(index = 1, key = "broker_id")
    var brokerId: String = "8"

    @Boom(index = 2, key = "rank_type")
    var rankType: String? = null

    @Boom(index = 3, key = "title")
    var title: String? = null

    @Boom(index = 4, isOptional = true, key = "tab_id")
    var selectedTabId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        IndexFuturesFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
