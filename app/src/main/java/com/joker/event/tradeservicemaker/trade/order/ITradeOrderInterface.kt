package com.joker.event.tradeservicemaker.trade.order

import com.joker.event.tradeservicemaker.trade.base.ITradeInterface

/**
 * 交易对外接口，订单模块
 * 包含子模块：订单记录、下单、IPO、定投、策略
 */
interface ITradeOrderInterface : ITradeOrderRecordInterface, ITradeOrderPlaceInterface,
    ITradeOrderIpoInterface, ITradeOrderRecurringInterface, ITradeInterface, ITradeOrderStrategyInterface
