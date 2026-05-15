package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "option_report_edit")
class OptionSellerColumnSettingFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.availableColumnStrIntentKey", desc = "不可见列")
    var availableColumnStr: String? = null

    @Boom(index = 1, key = "com.joker.event.router.curSelectColumnStrIntentKey", desc = "可见列")
    var curSelectColumnStr: String? = null

    @Boom(index = 2, key = "com.joker.event.router.allSelectColumnStrIntentKey", desc = "所有列")
    var allSelectColumnStr: String? = null

}