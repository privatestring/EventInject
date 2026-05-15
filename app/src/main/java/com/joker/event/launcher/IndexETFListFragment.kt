package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.etf.network.pojo.ETFCardTab
import launcher.Boom

class IndexETFListFragment : Fragment() {
    @Boom(index = 0)
    var regionId: Int = 0

    @Boom(index = 1)
    var etfCardTab: ETFCardTab? = null

    @Boom(index = 2)
    var rankType: String? = null

    @Boom(index = 3, isOptional = true, key = "ishketf")
    var isHKETF: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        IndexETFListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
