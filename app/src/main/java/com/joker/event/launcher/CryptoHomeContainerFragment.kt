package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class CryptoHomeContainerFragment : Fragment() {
    @Boom(index = 0, isOptional = true, key = "regionId")
    var regionId: String = ""

    @Boom(index = 1, isOptional = true, key = "order")
    var defaultOrder: String? = null

    @Boom(index = 2, isOptional = true, key = "isSelect")
    var isSelectModel: String? = null

    @Boom(index = 3, isOptional = true, key = "autoFinish")
    var isAutoFinish: String? = null

    @Boom(index = 4, isOptional = true, key = "standalone")
    var isStandalone: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CryptoHomeContainerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
