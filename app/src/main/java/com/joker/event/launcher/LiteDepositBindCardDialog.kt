package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class LiteDepositBindCardDialog : DialogFragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 99, isOptional = true)
    var type: Int = 0

    @Boom(index = 110, isOptional = true)
    var replaceable: Boolean = false

    @Boom(index = 111, isOptional = true)
    var isSupportRtpBindCard: Boolean = false

    @Boom(index = 112, isOptional = true)
    var isRtpProcess: Boolean = false

    @Boom(index = 113, isOptional = true, key = "source")
    var pageNameV2: String = ""

    @Boom(index = 114, isOptional = true)
    var needDepositDialogTip: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteDepositBindCardDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
