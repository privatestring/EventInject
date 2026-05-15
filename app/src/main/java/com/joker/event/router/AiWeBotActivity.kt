package com.joker.event.router

import android.app.Activity
import launcher.Boom
import launcher.Router

@Router(routerPath = "webotChat")
class AiWeBotActivity : Activity() {

    @Boom(index = 0, isOptional = true, desc = "场景参数, 参考getAiWebotActionUrl方法")
    var sceneParam: String? = null

    @Boom(index = 1, isOptional = true, desc = "内容元素是否需要透明度变化显示")
    var needAlphaAnim: String? = null

    @Boom(index = 2, isOptional = true, desc = "是否只是签协议")
    var onlySignAgreement: String? = null

}
