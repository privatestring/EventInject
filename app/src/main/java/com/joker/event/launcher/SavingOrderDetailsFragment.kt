package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class SavingOrderDetailsFragment : Fragment() {
    @Boom(index = 0)
    var accountKey: String = ""

    @Boom(index = 1)
    var orderId: String = ""

    @Boom(index = 2, isOptional = true)
    var side: String? = null

    @Boom(index = 3, isOptional = true)
    var tickerId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SavingOrderDetailsFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
