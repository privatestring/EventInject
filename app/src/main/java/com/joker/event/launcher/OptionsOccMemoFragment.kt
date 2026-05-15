package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.market.options.occ.viewdata.OptionsOccMemoItemData
import java.util.ArrayList
import launcher.Boom

class OptionsOccMemoFragment : Fragment() {
    @Boom(index = 0, isOptional = true, key = "regionId")
    var regionId: String = ""

    @Boom(index = 1)
    var rankType: String? = null

    @Boom(index = 2, isOptional = true)
    var defaultData: ArrayList<OptionsOccMemoItemData>? = null

    @Boom(index = 3)
    var isDetail: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        OptionsOccMemoFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
