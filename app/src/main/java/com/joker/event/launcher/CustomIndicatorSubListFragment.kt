package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.subchart.style.manager.IndicatorManagerViewModel
import launcher.Boom

class CustomIndicatorSubListFragment : Fragment() {
    @Boom(index = 0)
    var type: Int = 0

    @Boom(index = 1)
    var isCrypto: String = ""

    @Boom(index = 2, key = "key_is_trade")
    var isTrade: String = ""

    @Boom(index = 3)
    var settingJumpNewPage: String = ""

    @Boom(index = 4, isOptional = true)
    var indicatorManagerVMClass: Class<out IndicatorManagerViewModel>? = null

    @Boom(index = 5, isOptional = true)
    var selectOverlay: String? = null

    @Boom(index = 6, isOptional = true)
    var selectCategory = ArrayList<String>()

    @Boom(index = 7, isOptional = true)
    var vmNull: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CustomIndicatorSubListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
