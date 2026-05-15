package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "bindAutoUpdateTips")
class BindAutoUpdateTipsActivity : Activity() {

    @Boom(index = 0, key = "account_type", desc = "是否是绑定手机")
    var isBindPhone: String = ""

    @Boom(index = 1, key = "bind_guide_tips_action", desc = "绑定后动作的key")
    var keyBindGuideTipsAction: String = ""

    @Boom(index = 2, key = "is_bind", desc = "是否是新绑定")
    var isBind: String = ""

}