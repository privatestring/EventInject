package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class CreateAlertDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var price: String = ""

    @Boom(index = 1, isOptional = true)
    var priceBelow: String? = null

    @Boom(index = 2)
    var symbol: String = ""

    @Boom(index = 3, isOptional = true)
    var showPrice: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CreateAlertDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
