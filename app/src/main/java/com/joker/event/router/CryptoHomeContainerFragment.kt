package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "cryptoLandPage")
class CryptoHomeContainerFragment : Fragment() {

    @Boom(index = 0, key = "regionId", isOptional = true, desc = "regionId，不传默认为 REGION_US ")
    var regionId: String = ""

    @Boom(index = 1, key = "order", isOptional = true, desc = " 榜单默认排序字段")
    var defaultOrder: String? = null

    @Boom(index = 2, key = "isSelect", isOptional = true, desc = "是否是选择模式。如果为true，将会以 ticker 为 key，返回选中的 Ticker 对象，Router callback 方式跳转会返回 context to ticker 的 Pair")
    var isSelectModel: String? = null

    @Boom(index = 3, key = "autoFinish", isOptional = true, desc = "选择模式下选取标的后自动关闭页面。快捷交易进入选择标的需要先判断权限，待权限通过后跳转下单才能关闭当前页面。默认自动关闭")
    var isAutoFinish: String? = null

    @Boom(index = 4, key = "standalone", isOptional = true, desc = "是否是独立落地页")
    var isStandalone: String = ""

}