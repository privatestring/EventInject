package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class HostWebViewDialog : DialogFragment() {
    @Boom(index = 0)
    var loadUrl: String = ""

    @Boom(index = 1)
    var hRadio: Float = 1f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        HostWebViewDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
