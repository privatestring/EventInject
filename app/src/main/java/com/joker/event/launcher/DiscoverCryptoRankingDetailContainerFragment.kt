package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class DiscoverCryptoRankingDetailContainerFragment : Fragment() {
    @Boom(index = 0, isOptional = true, key = "regionId")
    var regionId: String = ""

    @Boom(index = 1, isOptional = true)
    var groupId: String = ""

    @Boom(index = 2, isOptional = true)
    var defaultId: String? = null

    @Boom(index = 3, isOptional = true)
    var title: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DiscoverCryptoRankingDetailContainerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
