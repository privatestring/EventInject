package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.group.bean.GroupBean
import launcher.Boom

class GroupPostListFragment : Fragment() {
    @Boom(index = 0)
    var groupUuid: String = ""

    @Boom(index = 1)
    var sortType: String = ""

    @Boom(index = 2)
    var groupBean: GroupBean? = null

    @Boom(index = 3, isOptional = true)
    var postUuid: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        GroupPostListFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
