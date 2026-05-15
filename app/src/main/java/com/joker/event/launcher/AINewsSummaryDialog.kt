package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.views.mask.MaskItem
import launcher.Boom

class AINewsSummaryDialog : DialogFragment() {
    @Boom(index = 0)
    var title: String? = null

    @Boom(index = 1)
    var summary: String? = null

    @Boom(index = 2)
    var maskItem: MaskItem? = null

    @Boom(index = 3)
    var pageType: String? = null

    @Boom(index = 4)
    var pageName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AINewsSummaryDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
