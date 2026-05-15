package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.jump.action.ParamConsts
import com.webull.rankstemplate.pojo.RanksTabData
import launcher.Boom

class RanksCollectDialogFragment : DialogFragment() {
    @Boom(index = 0, key = "regionId")
    var regionId: String? = null

    @Boom(index = 1, key = "groupId")
    var groupId: String? = null

    @Boom(index = 2, key = "groupType")
    var groupType: String? = null

    @Boom(index = 3, key = "tabList")
    var commonTabBeanList: ArrayList<RanksTabData>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        RanksCollectDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
