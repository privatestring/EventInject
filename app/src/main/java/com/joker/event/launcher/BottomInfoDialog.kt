package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class BottomInfoDialog : DialogFragment() {
    @Boom(index = 1)
    var title: CharSequence = ""

    @Boom(index = 2)
    var content: CharSequence = ""

    @Boom(index = 3, isOptional = true)
    var minimumHeight: Int? = null

    @Boom(index = 4, isOptional = true)
    var reportPage: String = "Common_Pop"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BottomInfoDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
