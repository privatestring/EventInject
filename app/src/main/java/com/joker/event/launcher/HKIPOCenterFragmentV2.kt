package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class HKIPOCenterFragmentV2 : Fragment() {
    @Boom(index = 0, isOptional = true)
    var mRegionId: Int = 0

    @Boom(index = 1, isOptional = true)
    var statusListStr: String = ""

    @Boom(index = 2, isOptional = true)
    var nameListStr: String = ""

    @Boom(index = 3, isOptional = true, key = "intent_key_select_status")
    var mDefaultStatus: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        HKIPOCenterFragmentV2Launcher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
