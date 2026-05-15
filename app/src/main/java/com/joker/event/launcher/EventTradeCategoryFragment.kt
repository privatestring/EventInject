package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class EventTradeCategoryFragment : Fragment() {
    @Boom(index = 0, key = "categoryId")
    var categoryId: String? = null

    @Boom(index = 1, isOptional = true, key = "eventTabId")
    var eventTabId: String? = null

    @Boom(index = 2, isOptional = true, key = "eventSubTabId")
    var eventSubTabId: String? = null

    @Boom(index = 3, isOptional = true, key = "eventName")
    var eventName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventTradeCategoryFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
