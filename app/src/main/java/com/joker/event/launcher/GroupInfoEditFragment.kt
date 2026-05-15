package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.group.groupcontrol.adapter.GroupType
import com.webull.group.grouplist.bean.Category
import launcher.Boom

class GroupInfoEditFragment : Fragment() {
    @Boom(index = 0)
    lateinit var groupUuid: String

    @Boom(index = 2)
    lateinit var groupType: GroupType

    @Boom(index = 1)
    lateinit var groupCategory: Category

    @Boom(index = 3)
    lateinit var groupProfile: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        GroupInfoEditFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
