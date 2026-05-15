package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.AchAcct
import com.webull.order.recurring.bean.PaymentMethod
import launcher.Boom

class PaymentMethodSelectDialog : DialogFragment() {
    @Boom(index = 1)
    var curSelect: PaymentMethod? = null

    @Boom(index = 2)
    var recurringBuyingPower: String? = null

    @Boom(index = 3)
    var achAcct: AchAcct? = null

    @Boom(index = 4)
    var currency: String? = null

    @Boom(index = 5)
    var mAccountInfo: AccountInfo? = null

    @Boom(index = 6)
    var mIsCrypto: Boolean? = false

    @Boom(index = 7)
    var supportACH: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PaymentMethodSelectDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
