package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.library.broker.common.position.PositionType
import com.webull.trade.asset.position.detail.stock.PositionFromType
import launcher.Boom

class UsWarrantPositionDetailsFragment : Fragment() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var positionId: String = ""

    @Boom(index = 3)
    var tickerId: String? = null

    @Boom(index = 4, isOptional = true)
    var positionType: PositionType = PositionType()

    @Boom(index = 5, isOptional = true)
    var fromType: PositionFromType = PositionFromType()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UsWarrantPositionDetailsFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
