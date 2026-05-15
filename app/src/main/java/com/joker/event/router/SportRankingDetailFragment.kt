package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "sport_ranking_detail")
class SportRankingDetailFragment : Fragment() {

    @Boom(index = 0, key = "sportRankingDetailTitle", desc = "标题")
    var title: String = ""

    @Boom(index = 1, key = "sportRankingDetailSelectTabId", desc = "当前选中的tabId")
    var tabId: String = ""

}