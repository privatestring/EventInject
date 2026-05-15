package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import launcher.Boom

class PasswordVerifyActivityV2 : Activity() {
    @Boom(index = 0, key = "result_key")
    var resultKey: String = "result_key"

    @Boom(index = 1, key = "mVerifyPwdTips")
    var mVerifyPwdTips: String = ""

    @Boom(index = 2, key = "stepCount")
    var stepCount: Int = 4

    @Boom(index = 3, key = "curStep")
    var curStep: Int = 0

    @Boom(index = 4, key = "biz_title")
    var bizTitle: String? = null

    @Boom(index = 5, key = "flowName")
    var flowName: String? = null

    @Boom(index = 6, key = "pageMargin")
    var pageMargin: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PasswordVerifyActivityV2Launcher.bind(this)
    }
}
