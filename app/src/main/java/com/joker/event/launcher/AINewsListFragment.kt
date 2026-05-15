package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.ArrayList
import launcher.Boom

class AINewsListFragment : Fragment() {
    @Boom(index = 0)
    var tickerIdList: ArrayList<String>? = null

    @Boom(index = 1)
    var type: String? = null

    @Boom(index = 2)
    var pageType: String? = null

    @Boom(index = 3)
    var isShowLikes: String? = null

    @Boom(index = 4, isOptional = true)
    var showAiSummary: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AINewsListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
