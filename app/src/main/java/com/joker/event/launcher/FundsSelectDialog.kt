package com.joker.event.launcher

import launcher.Boom

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/18
 */
class FundsSelectDialog {

    @Boom(index = 0)
    var title: String? = null

    @Boom(index = 1)
    var data: ArrayList<Triple<String, String?, Boolean>>? = null
}