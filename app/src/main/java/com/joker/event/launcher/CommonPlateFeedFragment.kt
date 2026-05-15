package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class CommonPlateFeedFragment : Fragment() {
    @Boom(index = 0)
    var code: String = ""

    @Boom(index = 1, isOptional = true)
    var adapterMode: Boolean = false

    @Boom(index = 2, isOptional = true)
    var pageSize: Int = 0

    @Boom(index = 3, isOptional = true)
    var showActionBar: Boolean = false

    @Boom(index = 4, isOptional = true)
    var title: String? = null

    @Boom(index = 5, isOptional = true)
    var showDividerItemDecoration: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CommonPlateFeedFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
