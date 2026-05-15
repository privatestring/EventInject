package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class SimpleBottomMenuDialog : DialogFragment() {
    @Boom(index = 0, isOptional = true)
    var showSelectItem = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SimpleBottomMenuDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
