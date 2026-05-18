package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.socialapi.beans.square.SquareFeedFilter
import com.webull.commonmodule.networkinterface.socialapi.beans.square.SquareFeedFilterOption
import java.util.ArrayList
import java.util.HashSet
import launcher.Boom

class SquareFeedFilterDialog : DialogFragment() {
    @Boom(index = 0)
    var filterList = hashSetOf<String>()

    @Boom(index = 1)
    var allTypes: ArrayList<SquareFeedFilter> = arrayListOf()

    @Boom(index = 2)
    var optionTypes: ArrayList<SquareFeedFilterOption> = arrayListOf()

    @Boom(index = 3)
    var sourcePage: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SquareFeedFilterDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
