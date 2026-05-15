package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "educationCenter")
class EducationCenterFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.supportActionBarIntentKey", isOptional = true, desc = "是否显示标题栏，默认显示")
    var supportActionBar: String? = null

}