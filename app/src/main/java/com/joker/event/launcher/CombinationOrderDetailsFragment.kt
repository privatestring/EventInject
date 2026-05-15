package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.trade.bean.NewOrder
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class CombinationOrderDetailsFragment : Fragment() {
    @Boom(index = 0)
    var mAccountInfo: AccountInfo? = null

    @Boom(index = 1)
    var comboId: String? = null

    @Boom(index = 2)
    var isOption: Boolean = false

    @Boom(index = 3)
    var order: NewOrder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CombinationOrderDetailsFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
