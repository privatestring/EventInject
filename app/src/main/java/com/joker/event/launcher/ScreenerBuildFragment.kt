package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class ScreenerBuildFragment : Fragment() {
    @Boom(key = "screener_json_string", index = 1)
    var mUpdateScreenerString: String = ""

    @Boom(key = "screener_id", index = 2)
    var mScreenerId: String = ""

    @Boom(key = "screener_name", index = 3)
    var mScreenerName: String = ""

    @Boom(key = "source", index = 4, isOptional = true)
    var mSource: String = ""

    @Boom(key = "screener_is_modify", index = 5, isOptional = true)
    var isModify: Boolean = false

    @Boom(key = "screener_type", index = 6)
    var mScreenerType: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ScreenerBuildFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
