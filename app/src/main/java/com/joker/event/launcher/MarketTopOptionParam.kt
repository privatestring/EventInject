package com.joker.event.launcher

import launcher.Boom

class MarketTopOptionParam {
    @Boom(index = 0, key = "regionId")
    var regionId: String = ""

    @Boom(index = 1, key = "tabId")
    var tabId: String = ""

    @Boom(index = 2, key = "groupId")
    var groupId: String = ""

    @Boom(index = 3, key = "groupType")
    var groupType: String = ""

    @Boom(index = 4, key = "title")
    var title: String = ""

    @Boom(index = 5, isOptional = true, key = "showTab")
    var showTab: String = "true"

}
