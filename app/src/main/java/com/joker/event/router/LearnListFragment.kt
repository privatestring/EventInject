package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "learn_list")
class LearnListFragment : Fragment() {

    @Boom(index = 0, key = "learn_type_id", desc = "搜索类型:1 course;2 lessons;3 plan;5 keyPoint")
    var learnType: String = ""

    @Boom(index = 1, key = "learn_key", desc = "搜索关键字")
    var searchKey: String = ""

}