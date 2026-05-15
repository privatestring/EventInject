package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.bean.TickerEntry
import launcher.Boom

class SearchRightDialogFragment : DialogFragment() {
    @Boom(index = 0)
    var tickerEntry: TickerEntry? = null

    @Boom(index = 1, isOptional = true)
    var isFitsSystemWindows: Boolean = true

    @Boom(index = 2, isOptional = true)
    var edgePadding: Int = 0

    @Boom(index = 3, isOptional = true)
    var hideSystemBar: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SearchRightDialogFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
