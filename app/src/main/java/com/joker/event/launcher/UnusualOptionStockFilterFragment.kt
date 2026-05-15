package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class UnusualOptionStockFilterFragment : Fragment() {
    @Boom(index = 0, key = "groupId")
    var groupId: String? = null

    @Boom(index = 1, key = "regionId")
    var regionId: String? = null

    @Boom(index = 2)
    var rule: String? = null

    @Boom(index = 3)
    var minValue: String? = null

    @Boom(index = 4)
    var maxValue: String? = null

    @Boom(index = 5, key = "title")
    var title: String? = null

    @Boom(index = 6)
    var valueUnit: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UnusualOptionStockFilterFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
