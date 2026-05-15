package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.OptionLeg
import java.util.ArrayList
import launcher.Boom

class OptionDiscoverSummaryBottomInfoDialog : DialogFragment() {
    @Boom(index = 1)
    var title: String = ""

    @Boom(index = 2)
    var briefDescription: String = ""

    @Boom(index = 3)
    var legList: ArrayList<OptionLeg> = arrayListOf()

    @Boom(index = 4)
    var quantity: String = ""

    @Boom(index = 5)
    var orderType: String = ""

    @Boom(index = 6)
    var orderPrice: String = ""

    @Boom(index = 7)
    var benefitDescription: String = ""

    @Boom(index = 8)
    var riskDescription: String = ""

    @Boom(index = 9)
    var probabilityPercentage: String = ""

    @Boom(index = 10)
    var probabilityDescription: String = ""

    @Boom(index = 11)
    var originStrategy: String = ""

    @Boom(index = 12)
    var symbol: String = ""

    @Boom(index = 13)
    var action: String = ""

    @Boom(index = 14)
    var quoteMultiplier: String = ""

    @Boom(index = 15)
    var optionAction: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionDiscoverSummaryBottomInfoDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
