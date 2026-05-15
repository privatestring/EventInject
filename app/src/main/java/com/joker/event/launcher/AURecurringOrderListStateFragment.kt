package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.library.broker.common.home.page.fragment.orders.recurring.bean.RecurringDetailInfo
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class AURecurringOrderListStateFragment : Fragment() {
    @Boom(index = 1)
    var mAccountInfo: AccountInfo = AccountInfo()

    @Boom(index = 2)
    var mPlanId: String = ""

    @Boom(index = 3)
    var hasMore: Boolean = false

    @Boom(index = 4)
    var detailInfo: RecurringDetailInfo? = null

    @Boom(index = 5)
    var headMarginTop: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AURecurringOrderListStateFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
