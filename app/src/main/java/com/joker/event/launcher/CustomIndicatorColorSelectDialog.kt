package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class CustomIndicatorColorSelectDialog : DialogFragment() {
    @Boom(index = 0)
    var color: Int = 0

    @Boom(index = 1)
    var lineWidth: Int = 1

    @Boom(index = 2)
    var alpha: Int = 100

    @Boom(index = 3)
    var showWidthAlpha: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CustomIndicatorColorSelectDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
