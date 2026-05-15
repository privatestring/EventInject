package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class FeeExplainDialog : DialogFragment() {
    @Boom(index = 0)
    var brokerId: Int = 0

    @Boom(index = 1)
    var currency: Int = 0

    @Boom(index = 2)
    var detailJson: String? = null

    @Boom(index = 3)
    var descStr: String? = null

    @Boom(index = 4, isOptional = true)
    var titleStr: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FeeExplainDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
