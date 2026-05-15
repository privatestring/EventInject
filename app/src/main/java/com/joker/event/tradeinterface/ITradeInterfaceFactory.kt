package com.webull.commonmodule.trade.service.trade.base

/**
 * 交易服务工厂接口（模拟真实项目）
 */
interface ITradeInterfaceFactory {
    fun <T : ITradeInterface> createInstance(clazz: Class<out T>): ITradeInterface?
}
