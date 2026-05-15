package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.Date
import launcher.Boom

class FutureCumulativePLDetailFragment : Fragment() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var startDate: String = ""

    @Boom(index = 3)
    var endDate: String = ""

    @Boom(index = 4)
    var lastSelectedType: Int = 0

    @Boom(index = 5)
    var currencyId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FutureCumulativePLDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
