package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "calendar_list_home")
class CalendarListContainerActivity : Activity() {

    @Boom(index = 0, key = "current_tab", isOptional = true, desc = "当前选中的Tab ID")
    var currentTabId: String? = null

}