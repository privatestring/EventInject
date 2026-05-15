package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.core.framework.service.services.portfolio.bean.WBPortfolio
import launcher.Boom

class LitePortfolioListDialog : DialogFragment() {
    @Boom(index = 0)
    var wbPortfolio: WBPortfolio? = null

    @Boom(index = 1, isOptional = true)
    var bottomTextColor: Int? = null

    @Boom(index = 2, isOptional = true)
    var selectedItemBgColor: Int? = null

    @Boom(index = 3, isOptional = true)
    var unSelectedItemBgColor: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LitePortfolioListDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
