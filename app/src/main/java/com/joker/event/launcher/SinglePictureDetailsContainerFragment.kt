package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.commonmodule.comment.ideas.viewmodel.PostItemViewModel
import launcher.Boom

class SinglePictureDetailsContainerFragment : Fragment() {
    @Boom(index = 1, key = "PostItemViewModel")
    var postItemViewModel: PostItemViewModel? = null

    @Boom(index = 2, key = "isSquare")
    var isSquare: Boolean = true

    @Boom(index = 3, key = "entrance")
    var entrance: String = "popular"

    @Boom(index = 4, isOptional = true, key = "superId")
    var superId: String = ""

    @Boom(index = 5, key = "sortType")
    var sortType: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        SinglePictureDetailsContainerFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
