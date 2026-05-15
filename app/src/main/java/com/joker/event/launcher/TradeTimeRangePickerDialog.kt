package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.datepick.bean.PLRangeBean
import launcher.Boom
import java.util.Date

class TradeTimeRangePickerDialog : DialogFragment() {
    @Boom(index = 1)
    var title: String? = null
    @Boom(index = 2)
    var startDate: Date? = null
    @Boom(index = 3)
    var endDate: Date? = Date()

    @Boom(index = 4, isOptional = true)
    var limitYearMax: Int? = null

    @Boom(index = 5, isOptional = true)
    var limitStr: String? = null

    @Boom(index = 6, isOptional = true)
    var yearLimit: Int? = null

    @Boom(index = 7, isOptional = true)
    var mIsSelectStart = true

    @Boom(index = 8, isOptional = true)
    var rangeBegin: PLRangeBean? = null

    @Boom(index = 9, isOptional = true)
    var minStartYear: Int? = null

    @Boom(index = 10, isOptional = true)
    var isGoneMonthView = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TradeTimeRangePickerDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
