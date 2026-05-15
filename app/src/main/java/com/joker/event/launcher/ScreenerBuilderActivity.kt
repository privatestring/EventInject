package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class ScreenerBuilderActivity : Activity() {
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
    var mScreenerType: String = ""

    @Boom(key = "open_type", index = 7, isOptional = true)
    var openType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenerBuilderActivityLauncher.bind(this)
    }
}
