package com.joker.event.update

import com.webull.core.framework.bean.TickerRealtimeV2
import wb.bean.AutoUpdate
import wb.bean.AutoUpdateIgnore

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/19
 */
@AutoUpdate
class TickerUpdateBean : TickerRealtimeV2() {
    var xxId: String = ""

    @AutoUpdateIgnore
    var xxId2: String = ""
}