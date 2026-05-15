package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class PreFindPasswordActivity : Activity() {
    @Boom(index = 0)
    var verifyType: Int = 0

    @Boom(index = 1)
    var account: String? = null

    @Boom(index = 2)
    var canByPhoneOrEmail: Boolean = true

    @Boom(index = 3)
    var isFromLogin: Boolean = true

    @Boom(index = 4)
    var applyType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreFindPasswordActivityLauncher.bind(this)
    }
}
