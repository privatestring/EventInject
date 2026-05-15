package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class AlertDatePickDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var dateStr: String = ""

    @Boom(index = 1)
    var position: Int = 0

    @Boom(index = 2, isOptional = true)
    var title: String = ""

    @Boom(index = 3, isOptional = true)
    var format: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AlertDatePickDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
