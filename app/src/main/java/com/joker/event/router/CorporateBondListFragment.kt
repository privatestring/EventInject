package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "corporate_bondList")
class CorporateBondListFragment : Fragment() {

    @Boom(index = 1, key = "title", isOptional = true, desc = "标题")
    var title: String? = null

    @Boom(index = 2, key = "forceJumpToBondCalc", desc = "是否强制跳转债券计算器")
    var forceJumpToBondCalc: String? = null

    @Boom(index = 3, key = "isEnterPopFilter", desc = "是否强制弹出/不弹筛选弹窗 0表示没有限制 1 强制弹出 -1 强制不弹")
    var isEnterPopFilter: String? = null

}