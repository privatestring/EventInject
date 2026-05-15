package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class LiteDepositTypeSelectDialog : DialogFragment() {
    @Boom(index = 0)
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 1, isOptional = true, key = "sourcePage")
    var sourcePage: String = ""

    @Boom(index = 99, isOptional = true, key = "com.joker.event.launcher.fromTypeIntentKey")
    var fromType: Int = 0

    @Boom(index = 110, isOptional = true, key = "selectId")
    var selectId: String = ""

    @Boom(index = 111, isOptional = true, key = "isRtpTransfer")
    var isRtpTransfer: Boolean = false

    @Boom(index = 112, isOptional = true, key = "excludeType")
    var excludeType: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LiteDepositTypeSelectDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
