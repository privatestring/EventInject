package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class CommentsAgreeDialog : DialogFragment() {
    @Boom(index = 0)
    var message: String = ""

    @Boom(index = 1)
    var title: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CommentsAgreeDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
