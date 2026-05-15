package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.bean.TickerKey
import launcher.Boom

class AITickerNewsListDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var tickerId: String? = null

    @Boom(index = 1)
    var type: String? = null

    @Boom(index = 2)
    var pageType: String? = null

    @Boom(index = 3)
    var isShowLikes: String? = null

    @Boom(index = 4)
    var tickerKey: TickerKey? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AITickerNewsListDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
