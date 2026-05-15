package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class Funds13FCollectionFragmentV2 : Fragment() {
    @Boom(index = 0, useFieldKey = true)
    var types: String? = null

    @Boom(index = 1, useFieldKey = true)
    var cik: String? = null

    @Boom(index = 2, useFieldKey = true)
    var filingId: String? = null

    @Boom(index = 3, useFieldKey = true)
    var mode: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Funds13FCollectionFragmentV2Launcher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
