package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.trade.order.common.FieldsObjV2
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.PlaceOrder
import launcher.Boom

class OrderConfirmDialog : DialogFragment() {
    @Boom(index = 0)
    var mAccountInfo: AccountInfo? = null

    @Boom(index = 1)
    var mPlaceOrder: PlaceOrder? = null

    @Boom(index = 2)
    var mFieldsObj: FieldsObjV2? = null

    @Boom(index = 3)
    var needShowFee: Boolean = true

    @Boom(index = 4)
    var needRequestFee: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OrderConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
