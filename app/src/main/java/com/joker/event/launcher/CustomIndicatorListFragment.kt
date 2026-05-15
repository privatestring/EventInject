package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.subchart.style.manager.IndicatorManagerViewModel
import launcher.Boom

class CustomIndicatorListFragment : Fragment() {
    @Boom(index = 0, key = "key_is_crypto")
    var isCrypto: String = ""

    @Boom(index = 1, key = "key_is_trade")
    var isTrade: String = ""

    @Boom(index = 2, key = "key_is_new_chart")
    var settingJumpNewPage: String = ""

    @Boom(index = 3, isOptional = true)
    var indicatorManagerVMClass: Class<out IndicatorManagerViewModel>? = null

    @Boom(index = 4, isOptional = true)
    var vmNull: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CustomIndicatorListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
