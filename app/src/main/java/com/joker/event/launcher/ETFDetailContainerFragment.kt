package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class ETFDetailContainerFragment : Fragment() {
    @Boom(index = 0, key = "title")
    var pageTitle: String = ""

    @Boom(index = 1, isOptional = true, key = "tabId")
    var tagId: String? = ""

    @Boom(index = 2, key = "groupId")
    var groupId: String? = ""

    @Boom(index = 3, isOptional = true, key = "regionId")
    var regionId: Int = 0

    @Boom(index = 4, isOptional = true)
    var isMultiCategory: Boolean = false

    @Boom(index = 5, isOptional = true)
    var isNeedChangeFilterPlace: Boolean = false

    @Boom(index = 6, isOptional = true)
    var isFromETFHomeV4: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ETFDetailContainerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
