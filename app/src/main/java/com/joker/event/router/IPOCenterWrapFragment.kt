package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "market_ipo_center")
class IPOCenterWrapFragment : Fragment() {

    @Boom(index = 0, key = "intent_key_show_switch_region", isOptional = true, desc = "是否展示区域切换开关")
    var showRegionSwitchStr: String = ""

    @Boom(index = 1, key = "intent_key_only_buying", isOptional = true, desc = "是否只展示申购中的IPO")
    var mShowBuyingString: String = ""

    @Boom(index = 2, key = "regionId", desc = "区域ID")
    var mCurRegionIdStr: String = ""

    @Boom(index = 3, key = "intent_key_title", desc = "标题")
    var mTitle: String? = null

    @Boom(index = 4, key = "intent_key_status_list", desc = "状态列表")
    var mStatusListStr: String? = null

    @Boom(index = 5, key = "intent_key_name_list", desc = "tab列表")
    var mNameListStr: String? = null

    @Boom(index = 6, key = "intent_key_select_status", desc = "当前选中的Item")
    var mDefaultStatus: String? = null

}