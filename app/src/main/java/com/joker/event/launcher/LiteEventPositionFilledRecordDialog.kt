package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.core.framework.bean.TickerBase
import launcher.Boom

class LiteEventPositionFilledRecordDialog : DialogFragment() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var positionId: String = ""

    @Boom(index = 4)
    var ticker: TickerBase? = null

    @Boom(index = 5)
    var marketId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteEventPositionFilledRecordDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
