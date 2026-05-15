package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.MakeResult

@MakeResult(includeStartForResult = true)
class DemoRequestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DemoRequestActivityLauncher.bind(this)
    }
}
