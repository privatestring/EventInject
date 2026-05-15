package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class WarrantFiltersDialogFragment : DialogFragment() {
    @Boom(index = 1)
    var dateValue: String = ""

    @Boom(index = 2)
    var orderStatus: String = ""

    @Boom(index = 3)
    var startTime: Long = -1

    @Boom(index = 4)
    var endTime: Long = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        WarrantFiltersDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
