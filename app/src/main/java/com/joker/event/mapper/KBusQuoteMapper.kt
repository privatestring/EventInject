package com.joker.event.mapper

import com.google.gson.reflect.TypeToken
import com.webull.core.framework.bean.TickerAskBid
import com.webull.core.framework.bean.TickerRealtimeV2
import com.webull.networkapi.utils.GsonUtils
import com.webull.order.place.framework.log.PlaceOrderLogNetTag
import com.webull.trade.bean.common.realtime.KBusQuoteBean
import com.webull.trade.common.logger.tradeLogE
import mapper.Mapper
import mapper.Mapping
import mapper.MappingConfig
import mapper.MappingIgnore

@Mapper
@MappingConfig()
/** 从交易侧预估接口返回的 KBus 行情数据转换为 TickerRealtimeV2 */
interface KBusQuoteMapper {

    @Mapping(source = "tradePrice", target = "price")
    @Mapping(source = "tradePrice", target = "pPrice")
    @Mapping(source = "tradeClose", target = "close")
    @Mapping(source = "nightPrice", target = "nPrice")
    @Mapping(expression = "java(getAskBidList(source.getAskList()))", target = "askList")
    @Mapping(expression = "java(getAskBidList(source.getBidList()))", target = "bidList")
    @Mapping(expression = "java(getAskBidList(source.getNightAskList()))", target = "nAskList")
    @Mapping(expression = "java(getAskBidList(source.getNightBidList()))", target = "nBidList")
    fun toBackupTickerRealtimeV2(source: KBusQuoteBean?): TickerRealtimeV2?

    @MappingIgnore
    fun getAskBidList(source: String?): ArrayList<TickerAskBid>? {
        try {
            if (source.isNullOrEmpty()) return null
            return GsonUtils.fromLocalJson(source, object : TypeToken<ArrayList<TickerAskBid>>() {}.type)
        } catch (e: Exception) {
            "KBusQuoteMapper error: ${e.message}".tradeLogE(PlaceOrderLogNetTag)
        }
        return null
    }
}