package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class BondFilterResultFragment : Fragment() {
    @Boom(index = 1, key = "regionId")
    var regionId: String? = null

    @Boom(index = 2, key = "title")
    var title: String? = null

    @Boom(index = 3, key = "forceJumpToCal")
    var forceJumpToBondCalc: String? = "false"

    @Boom(index = 4, key = "isEnterPopFilter")
    var isEnterPopFilter: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BondFilterResultFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
