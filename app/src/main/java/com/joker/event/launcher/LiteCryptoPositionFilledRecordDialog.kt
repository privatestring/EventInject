package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.broker.common.position.PositionType
import com.webull.trade.common.bean.crypto.CoinTickerBase
import launcher.Boom

class LiteCryptoPositionFilledRecordDialog : DialogFragment() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var positionId: String = ""

    @Boom(index = 4)
    var ticker: CoinTickerBase? = null

    @Boom(index = 5, isOptional = true)
    var positionType: PositionType? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteCryptoPositionFilledRecordDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
