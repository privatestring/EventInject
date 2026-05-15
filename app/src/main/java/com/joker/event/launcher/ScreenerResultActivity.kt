package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class ScreenerResultActivity : Activity() {
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
    var mScreenerType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenerResultActivityLauncher.bind(this)
    }
}
