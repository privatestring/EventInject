package com.webull.core.framework.bean

import com.google.gson.annotations.SerializedName
import wb.bean.AutoUpdate
import wb.bean.AutoUpdateIgnore

/** Stub: 行情实时数据（从 AppDev3 同步字段） */
@AutoUpdate(functionName = "updateJokerFields")
open class TickerRealtimeV2 : TickerBase() {
    var price: String? = null

    /** 盘前盘后价格 */
    @get:JvmName("getpPrice")
    @set:JvmName("setpPrice")
    @SerializedName(value = "pPrice", alternate = ["pprice"])
    var pPrice: String? = null
    var close: String? = null

    @AutoUpdateIgnore
    var open: String? = null
    var high: String? = null
    var low: String? = null
    var nPrice: String? = null
    var status: String? = null
    var askList: ArrayList<TickerAskBid>? = null
    var bidList: ArrayList<TickerAskBid>? = null
    var nAskList: ArrayList<TickerAskBid>? = null
    var nBidList: ArrayList<TickerAskBid>? = null
}
