package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import launcher.Boom

class FundRiskLevelNotMatchDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var brokerId: Int = 0

    @Boom(index = 1)
    var fundRiskLevel: Int = 1

    @Boom(index = 2)
    var accountRiskLevel: String = ""

    @Boom(index = 3)
    var allowChange: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FundRiskLevelNotMatchDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
