package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.commonmodule.comment.ideas.viewmodel.PostItemViewModel
import launcher.Boom

class PictureDetailsContainerActivity : Activity() {
    @Boom(index = 1, key = "PostItemViewModel")
    var postItemViewModel: PostItemViewModel? = null

    @Boom(index = 2, key = "modelHaseCode")
    var modelHaseCode: Int = -1

    @Boom(index = 3, key = "position")
    var position: Int = 0

    @Boom(index = 4, key = "pictureSource")
    var pictureSource: String = ""

    @Boom(index = 5, isOptional = true, key = "superId")
    var superId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PictureDetailsContainerActivityLauncher.bind(this)
    }
}
