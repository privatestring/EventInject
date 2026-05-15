package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "communitySetting")
class UserDetailSettingFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.userUUIDIntentKey", desc = "用户的唯一标识")
    var userUUID: String = ""

}