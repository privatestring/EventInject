package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.core.framework.bean.TickerRealtimeV2
import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import com.webull.trade.order.type.stock.def.OrderTypeEnum
import java.math.BigDecimal
import launcher.Boom

class FuturesTpslPlaceFragment : Fragment() {
    @Boom(index = 0)
    var quote: TickerRealtimeV2? = null

    @Boom(index = 1)
    var orderActionEnum: OrderActionEnum? = null

    @Boom(index = 2)
    var orderTypeEnum: OrderTypeEnum? = null

    @Boom(index = 3)
    var quantity: BigDecimal? = null

    @Boom(index = 4)
    var timeInForceEnum: TimeInForceEnum? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FuturesTpslPlaceFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
