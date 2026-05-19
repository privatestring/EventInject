package com.joker.event.update

import com.webull.core.framework.bean.TickerRealtimeV2
import wb.bean.AutoUpdate
import wb.bean.AutoUpdateAlways
import wb.bean.AutoUpdateCheck
import wb.bean.AutoUpdateIgnore

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/19
 */
@AutoUpdate
class TickerUpdateBean : TickerRealtimeV2() {
    // 默认类级别 stringCheck: valueIsNotEmpty()
    var xxId: String = ""

    @AutoUpdateAlways
    var allData: String = ""

    // @AutoUpdateIgnore: 跳过
    @AutoUpdateIgnore
    var xxId2: String = ""

    // String 自定义 condition
    @AutoUpdateCheck(condition = "{field} != null && {field}.isNotEmpty()")
    var xxId3: String = ""

    // Int 自定义 condition（替代默认 != 0）
    @AutoUpdateCheck(condition = "{field} > 0")
    var volume: Int = 0

    var volume2: Int = -10086
    var volume3: Long = 0
    var isX: Boolean = false
    var isX2: Boolean? = null
    var isX3: Boolean = true
    var isX4: Boolean = true

    // Long 自定义 condition（替代默认 != 0L）
    @AutoUpdateCheck(condition = "{field} != -1L")
    var timestamp: Long = 0L

    // Boolean 默认 SKIP，通过 @AutoUpdateCheck 强制生成
    @AutoUpdateCheck(condition = "{field}")
    var isPush: Boolean = false

    var ticker: TickerRealtimeV2 = TickerRealtimeV2()
}
