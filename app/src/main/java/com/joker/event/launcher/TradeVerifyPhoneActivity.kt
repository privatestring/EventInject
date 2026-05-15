package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class TradeVerifyPhoneActivity : Activity() {
    @Boom(key = "account_info", index = 0)
    var brokerId: Int = -1

    @Boom(key = "verify_type", index = 1)
    var privacyEvent: String = ""

    @Boom(key = "result_type_key", index = 2)
    var resultTypeKey: String = "result_type_key"

    @Boom(key = "result_value_key", index = 3)
    var resultValueKey: String = "result_value_key"

    @Boom(key = "biz_title", index = 4)
    var bizTitle: String = ""

    @Boom(key = "flow", index = 5, isOptional = true)
    var flowName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TradeVerifyPhoneActivityLauncher.bind(this)
    }
}
