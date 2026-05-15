package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "MutualFundsLandPage")
class MFHomeFragment : Fragment() {

    @Boom(index = 0, isOptional = true, desc = "regionId")
    var regionId: String = ""

}