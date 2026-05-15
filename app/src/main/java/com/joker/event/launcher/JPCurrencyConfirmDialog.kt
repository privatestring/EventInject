package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import java.math.BigDecimal
import launcher.Boom

class JPCurrencyConfirmDialog : DialogFragment() {
    @Boom(index = 0)
    var brokerId: Int = 0

    @Boom(index = 1)
    var mSourceCurrencyId: Int = 0

    @Boom(index = 2)
    var mTargetCurrencyId: Int = 0

    @Boom(index = 3)
    var mFromAmount: String? = null

    @Boom(index = 4)
    var mToAmount: String? = null

    @Boom(index = 5)
    var mShowRate: BigDecimal? = null

    @Boom(index = 6)
    var mServerRate: String? = null

    @Boom(index = 7)
    var mSignature: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        JPCurrencyConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
