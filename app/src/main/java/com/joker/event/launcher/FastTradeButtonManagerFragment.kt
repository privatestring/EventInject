package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.core.framework.bean.TickerBase
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class FastTradeButtonManagerFragment : Fragment() {
    @Boom(index = 0)
    var tickerInfo: TickerBase? = null

    @Boom(index = 1)
    var accountInfo: AccountInfo? = null

    @Boom(index = 2, isOptional = true)
    var isOption: Boolean = false

    @Boom(index = 3, isOptional = true)
    var isSimulated: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FastTradeButtonManagerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
