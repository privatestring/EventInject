package com.webull.core.framework.bean

/** Stub: 行情实时数据（从 AppDev3 同步字段） */
open class TickerRealtimeV2 : TickerBase() {
    var price: String? = null
    var pPrice: String? = null
    var close: String? = null
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
