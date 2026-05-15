package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.Date
import launcher.Boom

class CalendarEarningsListContainerFragment : Fragment() {
    @Boom(index = 0)
    var type: String? = null

    @Boom(index = 1)
    var listType: String? = null

    @Boom(index = 2, isOptional = true)
    var requestTimestampMillis: Long = 0L

    @Boom(index = 3, isOptional = true)
    var requestDate: String = ""

    @Boom(index = 4)
    var showDate: String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CalendarEarningsListContainerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
