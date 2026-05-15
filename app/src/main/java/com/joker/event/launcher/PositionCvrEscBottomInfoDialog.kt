package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class PositionCvrEscBottomInfoDialog : DialogFragment() {
    @Boom(index = 1)
    var title: CharSequence = ""

    @Boom(index = 2)
    var subContent1: CharSequence = ""

    @Boom(index = 3)
    var subTitle2: CharSequence = ""

    @Boom(index = 4)
    var subContent2: CharSequence = ""

    @Boom(index = 5, isOptional = true)
    var minimumHeight: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PositionCvrEscBottomInfoDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
