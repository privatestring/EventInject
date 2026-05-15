package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.quoteapi.beans.wefolio.WefolioTickerListData
import launcher.Boom

class WefolioContainerDialogFragment : DialogFragment() {
    @Boom(index = 0, key = "pageType")
    var pageType: String = ""

    @Boom(index = 1, isOptional = true, key = "current_wefolio_data")
    var currentWefolioData: WefolioTickerListData? = null

    @Boom(index = 2, isOptional = true, key = "wefolioId")
    var wefolioId: String? = null

    @Boom(index = 3, isOptional = true, key = "wefolio_param_data")
    var wefolioParamData: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        WefolioContainerDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
