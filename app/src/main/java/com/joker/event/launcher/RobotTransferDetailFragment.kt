package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class RobotTransferDetailFragment : Fragment() {
    @Boom(index = 0, key = "id")
    var id: String = ""

    @Boom(index = 1)
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 110, isOptional = true, key = "type")
    var type: String = ""

    @Boom(index = 111, isOptional = true)
    var fromSource: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        RobotTransferDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
