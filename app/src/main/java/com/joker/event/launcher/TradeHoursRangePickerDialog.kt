package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import java.util.Date
import launcher.Boom

class TradeHoursRangePickerDialog : DialogFragment() {
    @Boom(index = 1)
    var title: String? = null

    @Boom(index = 2)
    var startDate: Date? = null

    @Boom(index = 3)
    var endDate: Date? = null

    @Boom(index = 4, isOptional = true)
    var mIsSelectStart: Boolean = true

    @Boom(index = 5, isOptional = true)
    var minStartTime: Date? = null

    @Boom(index = 6, isOptional = true)
    var maxEndTime: Date? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TradeHoursRangePickerDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
