package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class DepositConfirmDialog : DialogFragment() {
    @Boom(index = 0)
    var amount: String? = null

    @Boom(index = 1)
    var account: String? = null

    @Boom(index = 2)
    var estArrivalTime: String? = null

    @Boom(index = 3)
    var limit: String? = null

    @Boom(index = 4)
    var fee: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DepositConfirmDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
