package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.Date
import launcher.Boom

class TickerBondsProfitFragment : Fragment() {
    @Boom(index = 1)
    var secAccountId: Long = 0L

    @Boom(index = 2)
    var tikerId: String ? = null

    @Boom(index = 3)
    var startDate: String? = null

    @Boom(index = 4)
    var endDate: String? = null

    @Boom(index = 5)
    var durationString: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TickerBondsProfitFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
