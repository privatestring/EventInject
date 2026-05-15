package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class UserCenterCommonBottomDialogFragment : DialogFragment() {
    @Boom(index = 0, useFieldKey = true)
    var titleString: String = ""

    @Boom(index = 1, useFieldKey = true)
    var contentString: String = ""

    @Boom(index = 2, useFieldKey = true)
    var leftButtonText: String = ""

    @Boom(index = 3, useFieldKey = true)
    var rightButtonText: String = ""

    @Boom(index = 4, useFieldKey = true)
    var isShowCancel: Boolean = true

    @Boom(index = 5, useFieldKey = true)
    var isOnBackPressedCancel: Boolean = true

    @Boom(index = 6, isOptional = true, useFieldKey = true)
    var pageName: String = ""

    @Boom(index = 7, isOptional = true, useFieldKey = true)
    var sourcePage: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UserCenterCommonBottomDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
