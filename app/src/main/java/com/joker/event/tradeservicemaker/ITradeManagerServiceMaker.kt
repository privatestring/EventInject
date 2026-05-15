package com.joker.event.tradeservicemaker

import com.joker.event.tradeservicemaker.trade.base.ITradeInterface
import launcher.TradeServiceMaker

/**
 * 用于触发 ITradeManagerService 接口自动生成的标记接口
 *
 * 注解处理器会：
 * 1. 扫描 scanPackages 中指定的包，找出所有继承自 ITradeInterface 的接口
 * 2. 分析继承关系，找出顶层大接口
 * 3. 生成 ITradeManagerService 接口，自动继承所有顶层大接口 + IService
 *
 * 预期生成的顶层接口（按字母排序）：
 * - ITradeAccountInterface
 * - ITradeAssetInterface
 * - ITradeCoreInterface
 * - ITradeGlobalInterface
 * - ITradeOrderInterface
 * - ITradeWealthInterface
 * - IService (additional)
 */
@TradeServiceMaker(
    baseInterface = ITradeInterface::class,
    scanPackages = ["com.joker.event.tradeservicemaker.trade"],
    additionalInterfaces = [IService::class],
    packageName = "com.joker.event.tradeservicemaker",
    className = "ITradeManagerService"
)
interface ITradeManagerServiceMaker
