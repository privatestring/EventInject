package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "purchaseGroupList")
class SubscriptionProductsActivityV2 : Activity() {

    @Boom(index = 0, key = "defaultTab", desc = "默认展示的页索引：my:我的权限，market:市场行情, 13f:第3个tab，13f, wefolio: wefolio tab")
    var defaultTab: String = ""

}