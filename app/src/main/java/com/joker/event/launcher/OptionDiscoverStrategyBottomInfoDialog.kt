package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class OptionDiscoverStrategyBottomInfoDialog : DialogFragment() {
    @Boom(index = 1)
    var title: String = ""

    @Boom(index = 2)
    var firstContent: String = ""

    @Boom(index = 3)
    var secondContent: String = ""

    @Boom(index = 4)
    var firstContentTitle: String = ""

    @Boom(index = 5)
    var secondContentTitle: String = ""

    @Boom(index = 6, isOptional = true)
    var strategy: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionDiscoverStrategyBottomInfoDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
