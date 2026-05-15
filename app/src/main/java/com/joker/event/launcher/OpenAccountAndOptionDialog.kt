package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class OpenAccountAndOptionDialog : DialogFragment() {
    @Boom(index = 1)
    var title: String = ""

    @Boom(index = 2)
    var firstContent: String = ""

    @Boom(index = 3)
    var secondContent: String = ""

    @Boom(index = 4)
    var leftButtonText: String = ""

    @Boom(index = 5)
    var rightButtonText: String = ""

    @Boom(index = 6)
    var iconAttrId: Int = 0

    @Boom(index = 7)
    var needHideLeft: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OpenAccountAndOptionDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
