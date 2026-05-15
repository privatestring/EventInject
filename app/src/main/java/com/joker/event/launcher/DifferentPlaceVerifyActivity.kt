package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.accountmodule.network.bean.login.ResitrationUserInfo
import com.webull.accountmodule.network.bean.login.ThirdLoginUserRequest
import com.webull.commonmodule.networkinterface.userapi.beans.PassportUser
import launcher.Boom

class DifferentPlaceVerifyActivity : Activity() {
    @Boom(index = 0, useFieldKey = true)
    var phone: String? = null

    @Boom(index = 1, useFieldKey = true)
    var desensitizedPhone: String? = null

    @Boom(index = 2, useFieldKey = true)
    var isFromGuide: Boolean? = null

    @Boom(index = 3, useFieldKey = true)
    var questionUrl: String? = null

    @Boom(index = 4, useFieldKey = true)
    var thirdLoginUserRequest: ThirdLoginUserRequest? = null

    @Boom(index = 5, useFieldKey = true)
    var resitrationUserInfo: ResitrationUserInfo? = null

    @Boom(index = 6, useFieldKey = true)
    var passportUser: PassportUser? = null

    @Boom(index = 7, useFieldKey = true)
    var protocolParams: String? = null

    @Boom(index = 8, useFieldKey = true)
    var deviceAuthId: String? = null

    @Boom(index = 9, useFieldKey = true)
    var deviceAuthType: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DifferentPlaceVerifyActivityLauncher.bind(this)
    }
}
