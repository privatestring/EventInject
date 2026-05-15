package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class DemoActivity : Activity() {
    @Boom(index = 0, key = "this is Custom key")
    var param1: String = ""

    @Boom(index = 1, isOptional = true)
    var param2: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DemoActivityLauncher.bind(this)
    }
}
