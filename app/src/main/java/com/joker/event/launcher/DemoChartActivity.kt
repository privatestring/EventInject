package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class DemoChartActivity : Activity() {
    @Boom(index = 0, key = "this is Custom key")
    var param1: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DemoChartActivityLauncher.bind(this)
    }
}
