package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.OptionLeg
import com.webull.library.tradenetwork.bean.AccountInfo
import java.util.ArrayList
import launcher.Boom
import launcher.ParentCls
@ParentCls
class OptionPreviewAndSwitchDialogFragment : DialogFragment() {
    @Boom(index = 1)
    var tickerId: String = ""
    @Boom(index = 2)
    var tickerType: String? = null
    @Boom(index = 3)
    var stockPrice: String? = null
    @Boom(index = 4)
    var action: String? = null
    @Boom(index = 5)
    var limitPrice: String? = null
    @Boom(index = 6)
    var strategy: String? = null
    @Boom(index = 7)
    var accountInfo: AccountInfo? = null
    @Boom(index = 8)
    var optionLegs: ArrayList<OptionLeg>? = null
    @Boom(index = 9, isOptional = true)
    var quantity: String = "1"
    @Boom(index = 10, key = "orderType")
    var orderType: String? = null
    @Boom(index = 11, isOptional = true, key = "disableAllAction")
    var disableAllAction: Boolean = false
    @Boom(index = 12, isOptional = true, key = "isOptionDiscover")
    var isOptionDiscover: Boolean = false
    @Boom(index = 13, isOptional = true, key = "originStrategyName")
    var originStrategyName: String = ""
    @Boom(index = 14, isOptional = true, key = "isOptionRolling")
    var isOptionRolling: Boolean = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionPreviewAndSwitchDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
