package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class UsFundScreenerBuildFragment : Fragment() {
    @Boom(index = 0, isOptional = true)
    var filterType: Int? = null

    @Boom(index = 1, isOptional = true)
    var filterId: String? = null

    @Boom(index = 2)
    var isModify: Boolean = false

    @Boom(index = 3, key = "open_type", isOptional = true)
    var openType: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UsFundScreenerBuildFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
