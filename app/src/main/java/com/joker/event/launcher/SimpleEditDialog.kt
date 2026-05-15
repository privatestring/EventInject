package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class SimpleEditDialog : DialogFragment() {
    @Boom(index = 0)
    var editPageUrl: String = ""

    @Boom(index = 1)
    var requestCode: Int = 0

    @Boom(index = 2)
    var draftId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SimpleEditDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
