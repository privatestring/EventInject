package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.library.tradenetwork.bean.AccountInfo
import java.util.Date
import launcher.Boom

class AuCnoteItemListFragment : Fragment() {
    @Boom(index = 1)
    var tradeDate: String? = null

    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 2)
    var count: Int? = null

    @Boom(index = 4)
    var tradeDateStr: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AuCnoteItemListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
