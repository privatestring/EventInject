package com.joker.event.tradeservicemaker.trade.asset

import com.joker.event.tradeservicemaker.trade.base.ITradeInterface

/**
 * 交易对外接口，资产模块
 * 包含子模块：资产信息、持仓、出入金转账、盈亏、税务
 */
interface ITradeAssetInterface : ITradeAssetInfoInterface, ITradeAssetPositionInterface,
    ITradeAssetTransferInterface, ITradeAssetProfitLossInterface, ITradeAssetTaxInterface,
    ITradeInterface
