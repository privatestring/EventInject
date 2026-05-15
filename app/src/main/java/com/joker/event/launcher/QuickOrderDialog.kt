package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.alert.technical.fragment.bean.MessageQuickOrderBean
import com.webull.commonmodule.bean.TickerKey
import launcher.Boom

class QuickOrderDialog : DialogFragment() {
    @Boom(index = 1)
    var tickerKey: TickerKey = TickerKey()
    @Boom(index = 2)
    var messageQuickOrderBean: MessageQuickOrderBean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        QuickOrderDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
