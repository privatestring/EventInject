package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class PasswordVerifyFragment : Fragment() {
    @Boom(index = 0, key = "mVerifyPwdTips")
    var mVerifyPwdTips: String = ""

    @Boom(index = 1, key = "flowName")
    var flowName: String? = null

    @Boom(index = 2, key = "pageMargin")
    var pageMargin: Int? = null

    @Boom(index = 3, isOptional = true, key = "bizTitle")
    var bizTitle: String? = null

    @Boom(index = 4, isOptional = true, key = "supportActionBar")
    var mSupportActionBar: Boolean? = null

    @Boom(index = 5, isOptional = true, key = "resultKey")
    var resultKey: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PasswordVerifyFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
