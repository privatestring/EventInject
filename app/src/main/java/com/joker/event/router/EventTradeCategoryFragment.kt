package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "event_category_detail")
class EventTradeCategoryFragment : Fragment() {

    @Boom(index = 0, key = "categoryId", desc = "分组的ID")
    var categoryId: String? = null

    @Boom(index = 1, key = "eventTabId", isOptional = true, desc = "一级分组的ID")
    var eventTabId: String? = null

    @Boom(index = 2, key = "eventSubTabId", isOptional = true, desc = "二级分组的ID")
    var eventSubTabId: String? = null

    @Boom(index = 3, key = "eventName", isOptional = true, desc = "标题")
    var eventName: String? = null

}