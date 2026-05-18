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
    /** 这是key **/
    @Boom(index = 1)
    var accountKey: String = ""

    // 这是id
    @Boom(index = 2)
    var positionId: String = ""

    @Boom(index = 3, desc = "ticker标识")
    var tickerId: String? = null

    /**
     * 这是type
     */
    @Boom(index = 4, isOptional = true)
    var positionType: PositionType = PositionType()

    @Boom(index = 5, isOptional = true)
    var fromType: PositionFromType = PositionFromType() //小组类型

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UsWarrantPositionDetailsFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
