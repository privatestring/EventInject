package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "loginBind")
class LoginBindGuideActivity : Activity() {

    @Boom(index = 0, key = "is_login_by_phone", desc = "是否是手机登录")
    var isLoginByPhoneStr: String? = null

}