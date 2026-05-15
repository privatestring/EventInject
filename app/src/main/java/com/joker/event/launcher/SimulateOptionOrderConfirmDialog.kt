package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.tradenetwork.bean.AccountInfo
import com.webull.library.tradenetwork.bean.request.OptionOrderRequest
import launcher.Boom

class SimulateOptionOrderConfirmDialog : DialogFragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var request: OptionOrderRequest? = null

    @Boom(index = 2)
    var openOrClose: String? = null

    @Boom(index = 3)
    var isSimplePlaceOptionMode: Boolean = false

    @Boom(index = 4)
    var dialogWidth: Int = 0

    @Boom(index = 5)
    var userOrderPrice: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SimulateOptionOrderConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
