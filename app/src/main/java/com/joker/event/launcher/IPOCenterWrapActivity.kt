package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class IPOCenterWrapActivity : Activity() {
    @Boom(index = 1, isOptional = true, key = "intent_key_show_switch_region")
    var showRegionSwitchStr: String = ""

    @Boom(index = 2, isOptional = true, key = "intent_key_only_buying")
    var mShowBuyingString: String = ""

    @Boom(index = 3, key = "regionId")
    var mCurRegionIdStr: String = "0"

    @Boom(index = 4, key = "intent_key_title")
    var mTitle: String? = null

    @Boom(index = 5, key = "intent_key_status_list")
    var mStatusListStr: String? = null

    @Boom(index = 6, key = "intent_key_name_list")
    var mNameListStr: String? = null

    @Boom(index = 7, key = "intent_key_select_status")
    var mDefaultStatus: String? = null

    @Boom(index = 8, key = "index")
    var index: String = "6"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IPOCenterWrapActivityLauncher.bind(this)
    }
}
