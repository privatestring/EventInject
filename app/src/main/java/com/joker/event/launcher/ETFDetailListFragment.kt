package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.market.etf.bean.ETFDetailTagData
import launcher.Boom

class ETFDetailListFragment : Fragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String = ""

    @Boom(index = 1, key = "tabId")
    var tagId: String? = null

    @Boom(index = 2)
    var etfDetailTagData: ETFDetailTagData? = null

    @Boom(index = 3, isOptional = true)
    var isNeedChangeFilterPlace: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ETFDetailListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
