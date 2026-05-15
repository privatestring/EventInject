package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.core.framework.bean.TickerBase
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.AchAcct
import com.webull.order.recurring.bean.RecurringFieldsObject
import com.webull.wefolio.pojo.TradeWefolioInfo
import java.util.Date
import launcher.Boom

class RecurringOrderConfirmDialog : DialogFragment() {
    @Boom(index = 1)
    var mAccountInfo: AccountInfo = AccountInfo()

    @Boom(index = 2)
    var mFieldsObj: RecurringFieldsObject = RecurringFieldsObject()

    @Boom(index = 3)
    var mTicker: TickerBase? = null

    @Boom(index = 4)
    var mOrderDate: String? = null

    @Boom(index = 5)
    var achAcct: AchAcct? = null

    @Boom(index = 6)
    var mTradeWefolioInfo: TradeWefolioInfo? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        RecurringOrderConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
