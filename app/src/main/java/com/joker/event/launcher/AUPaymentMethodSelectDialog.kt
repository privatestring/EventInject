package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.tradenetwork.bean.BankCardAccountInfo
import java.util.ArrayList
import launcher.Boom

class AUPaymentMethodSelectDialog : DialogFragment() {
    @Boom(index = 1)
    var selectedPaymentId: String? = null

    @Boom(index = 2)
    var recurringBuyingPower: String? = null

    @Boom(index = 3)
    var bankCardList: ArrayList<BankCardAccountInfo>? = null

    @Boom(index = 4)
    var currency: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AUPaymentMethodSelectDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
