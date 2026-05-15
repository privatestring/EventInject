package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.library.tradenetwork.bean.CapitalDetailsResponse
import java.util.ArrayList
import java.util.HashMap
import launcher.Boom

class FundsFiltersDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var brokerId: Int = 0

    @Boom(index = 1)
    var group: ArrayList<CapitalDetailsResponse.FilterConditionGroupBean>? = null

    @Boom(index = 2)
    var selectMap: HashMap<String, List<CapitalDetailsResponse.FilterConditionItemBean>>? = null

    @Boom(index = 3)
    var mLastStartTime: Long = -1

    @Boom(index = 4)
    var mLastEndTime: Long = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        FundsFiltersDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
