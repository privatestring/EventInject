package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class OptionRollingModeChooseDialog : DialogFragment() {
    @Boom(index = 0)
    var title: String? = null

    @Boom(index = 1)
    var descAllLegTitle: String? = null

    @Boom(index = 2)
    var descAllLegDesc: String? = null

    @Boom(index = 3)
    var descSingleLegTitle: String? = null

    @Boom(index = 4)
    var descSingleLegDesc: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionRollingModeChooseDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
