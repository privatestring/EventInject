package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "portfolio_blueprint_guide")
class PortfolioBlueprintGuideFragment : Fragment() {

    @Boom(index = 0, key = "key_JUMP_URL", desc = "跳转的URL")
    var jumpUrl: String? = null

}