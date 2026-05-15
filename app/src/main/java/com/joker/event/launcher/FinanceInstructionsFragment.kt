package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class FinanceInstructionsFragment : Fragment() {
    @Boom(index = 0)
    var tickerId: String? = null

    @Boom(index = 1)
    var type: Int? = null

    @Boom(index = 2, isOptional = true)
    var factors: String? = null

    @Boom(index = 3, isOptional = true)
    var fiscalYear: Int? = null

    @Boom(index = 4, isOptional = true)
    var fiscalPeriod: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FinanceInstructionsFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
