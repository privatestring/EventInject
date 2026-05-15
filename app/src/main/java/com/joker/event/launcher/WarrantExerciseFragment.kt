package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.trade.bean.NewPosition
import launcher.Boom

class WarrantExerciseFragment : Fragment() {
    @Boom(index = 1)
    var accountKey: String = ""

    @Boom(index = 2)
    var tickerId: String = ""

    @Boom(index = 3)
    var exerciseQuantity: String = ""

    @Boom(index = 4)
    var positionBean: NewPosition? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        WarrantExerciseFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
