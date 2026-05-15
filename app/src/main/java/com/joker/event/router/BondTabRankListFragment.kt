package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "bond_tab_rank")
class BondTabRankListFragment : Fragment() {

    @Boom(index = 0, key = "current_tab", isOptional = true, desc = "当前选中的Tab ID")
    var currentTabId: String? = null

}