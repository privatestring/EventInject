package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.ArrayList
import launcher.Boom

class PorfolioFunctionFragment : Fragment() {
    @Boom(index = 0)
    var pageIndex: Int? = 0

    @Boom(index = 1)
    var tickerIdList: ArrayList<String> = arrayListOf()

    @Boom(index = 2)
    var upDownCountString: String? = null

    @Boom(index = 3)
    var portfolioId: Int? = null

    @Boom(index = 4)
    var isAiNews: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PorfolioFunctionFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
