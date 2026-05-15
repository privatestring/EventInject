package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class WpCryptoCoinOutSmsDialog : DialogFragment() {
    @Boom(index = 0)
    var uuid: String = ""

    @Boom(index = 1)
    var accountKey: String? = null

    @Boom(index = 2)
    var pwdType: Int? = null

    @Boom(index = 3)
    var pwd: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        WpCryptoCoinOutSmsDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
