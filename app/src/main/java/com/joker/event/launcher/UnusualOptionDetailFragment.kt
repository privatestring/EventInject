package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class UnusualOptionDetailFragment : Fragment() {
    @Boom(index = 0, key = "groupId")
    var groupId: String = ""

    @Boom(index = 1, key = "groupType")
    var groupType: String = ""

    @Boom(index = 2, key = "regionId")
    var regionId: String? = null

    @Boom(index = 3, isOptional = true, key = "title")
    var title: String? = null

    @Boom(index = 4, key = "rankType")
    var rankType: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UnusualOptionDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
