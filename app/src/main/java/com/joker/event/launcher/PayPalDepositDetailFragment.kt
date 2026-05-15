package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class PayPalDepositDetailFragment : Fragment() {
    @Boom(index = 0)
    var id: String? = null

    @Boom(index = 1)
    var secAccountId: Long? = null

    @Boom(index = 2, isOptional = true)
    var showHistoryMenu: Boolean = false

    @Boom(index = 3, isOptional = true)
    var showBackMenu: Boolean = false

    @Boom(index = 4, isOptional = true)
    var showBottomButton: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PayPalDepositDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
