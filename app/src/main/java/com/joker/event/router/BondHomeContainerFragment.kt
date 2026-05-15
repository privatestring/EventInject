package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "bondIndex")
class BondHomeContainerFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.titleIntentKey", desc = "标题")
    var title: String? = null

}