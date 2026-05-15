package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "moreLearnList")
class LessonsFragment : Fragment() {

    @Boom(index = 0, key = "sceneCode", isOptional = true, desc = "场景化获取投教内容的标记，目前包括了-> Scene_Options获取期权；Scene_futures获取期货；Scene_Recurring获取定投；Scene_Home_Page获取首页推荐；Scene_bond获取债券")
    var tagCode: String? = null

    @Boom(index = 1, key = "title", isOptional = true, desc = "页面标题定制化")
    var title: String? = null

}