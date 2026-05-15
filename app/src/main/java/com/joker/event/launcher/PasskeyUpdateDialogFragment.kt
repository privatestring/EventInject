package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class PasskeyUpdateDialogFragment : DialogFragment() {
    @Boom(index = 0, useFieldKey = true)
    var title: String = ""

    @Boom(index = 1, useFieldKey = true)
    var content: String = ""

    @Boom(index = 2, useFieldKey = true)
    var leftButtonText: String = ""

    @Boom(index = 3, useFieldKey = true)
    var rightButtonText: String = ""

    @Boom(index = 4, useFieldKey = true)
    var hasLottieAnim: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PasskeyUpdateDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
