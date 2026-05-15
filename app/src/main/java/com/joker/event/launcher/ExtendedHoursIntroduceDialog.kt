package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class ExtendedHoursIntroduceDialog : DialogFragment() {
    @Boom(index = 0)
    var accountKey: String? = null

    @Boom(index = 1)
    var supportExtendHours: Boolean = false

    @Boom(index = 2)
    var supportOvernight: Boolean = false

    @Boom(index = 3, isOptional = true)
    var isConditionOrderMode: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ExtendedHoursIntroduceDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
