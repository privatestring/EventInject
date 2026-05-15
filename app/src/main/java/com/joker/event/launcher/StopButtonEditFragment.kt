package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.core.framework.bean.TickerBase
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.trade.stock.fasttrade.repository.data.stock.FastTradeButtonConfigData
import launcher.Boom

class StopButtonEditFragment : Fragment() {
    @Boom(index = 0)
    var ticker: TickerBase? = null

    @Boom(index = 1)
    var account: AccountInfo? = null

    @Boom(index = 2)
    var configData: FastTradeButtonConfigData? = null

    @Boom(index = 3, isOptional = true)
    var isSimulated: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        StopButtonEditFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
