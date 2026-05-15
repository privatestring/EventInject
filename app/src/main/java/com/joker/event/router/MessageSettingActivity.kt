package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "message_setting")
class MessageSettingActivity : Activity() {

    @Boom(index = 0, key = "key_message_type", desc = "消息类型")
    var type: String? = null

}