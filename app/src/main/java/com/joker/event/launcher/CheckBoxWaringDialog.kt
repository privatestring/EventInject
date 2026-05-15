package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import java.util.ArrayList
import com.webull.order.dependency.api.common.response.OrderCheckResponse
import launcher.Boom

class CheckBoxWaringDialog : DialogFragment() {
    @Boom(index = 1)
    var checkResults: ArrayList<OrderCheckResponse.CheckResult?>? = null

    @Boom(index = 2)
    var isWithdrawStyle: Boolean = false

    @Boom(index = 3, isOptional = true)
    var confirm: Boolean = true

    @Boom(index = 4, isOptional = true)
    var showBack: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CheckBoxWaringDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
