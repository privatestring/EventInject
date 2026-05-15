package com.joker.event.tradeservicemaker.trade.account

import com.joker.event.tradeservicemaker.trade.base.ITradeInterface

/**
 * 交易对外接口，账户模块
 * 包含子模块：账户信息、权限、协议
 */
interface ITradeAccountInterface : ITradeAccountInfoInterface, ITradeAccountPermissionInterface,
    ITradeAccountAgreementInterface, ITradeInterface
