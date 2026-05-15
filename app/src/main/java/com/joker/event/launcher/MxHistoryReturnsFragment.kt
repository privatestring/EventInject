package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.networkinterface.securitiesapi.beans.FundsPerformanceResponse.FundPerformanceViewModel
import java.util.ArrayList
import launcher.Boom

class MxHistoryReturnsFragment : Fragment() {
    @Boom(index = 0)
    var tickerId: String = ""

    @Boom(index = 1)
    var template: String? = null

    @Boom(index = 2)
    var currencyId: Int? = null

    @Boom(index = 3, isOptional = true)
    var historyList: ArrayList<FundPerformanceViewModel>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MxHistoryReturnsFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
