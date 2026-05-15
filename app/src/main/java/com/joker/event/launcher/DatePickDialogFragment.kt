package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.datepick.bean.DateType
import launcher.Boom
import java.util.Date

class DatePickDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var type = DateType.CN_DATE

    @Boom(index = 1)
    var startDate = Date()

    @Boom(index = 2)
    var yearLimit = 200

    @Boom(index = 3)
    var isShowDateBefore = true

    @Boom(index = 5)
    var isCyclic = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DatePickDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
