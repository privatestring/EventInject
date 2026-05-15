package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class IPOCenterWrapFragment : Fragment() {
    @Boom(index = 1, isOptional = true, key = "intent_key_show_switch_region")
    var showRegionSwitchStr: String = ""

    @Boom(index = 2, isOptional = true, key = "intent_key_only_buying")
    var mShowBuyingString: String = ""

    @Boom(index = 3, key = "regionId")
    var mCurRegionIdStr: String = "6"

    @Boom(index = 4, key = "intent_key_title")
    var mTitle: String? = null

    @Boom(index = 5, key = "intent_key_status_list")
    var mStatusListStr: String? = null

    @Boom(index = 6, key = "intent_key_name_list")
    var mNameListStr: String? = null

    @Boom(index = 7, key = "intent_key_select_status")
    var mDefaultStatus: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        IPOCenterWrapFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
