package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class ScreenerResultFragment : Fragment() {
    @Boom(key = "key_strategy_id", index = 3)
    var mStrategyId: String = ""

    @Boom(key = "key_rules_map_jsonstr", index = 4)
    var strategy: String = ""

    @Boom(key = "key_rule_name", index = 5)
    var mTitle: String = ""

    @Boom(key = "source", index = 6)
    var mSource: String = ""

    @Boom(key = "screener_is_modify", index = 7, isOptional = true)
    var isModify: Boolean = false

    @Boom(key = "screener_type", index = 8)
    var mScreenerType: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ScreenerResultFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
