package com.webull.library.repository.constant

import java.io.Serializable

/** 订单方向 */
enum class OrderActionEnum(val constant: String, val desc: Int) {
    // 买入
    Buy("buy", 0),

    // 卖出
    Sell("sell", 0),

    // 卖空
    Short("xxx", 0),
    ;

    fun isBuy() : Boolean {
        return this == Buy
    }

    fun isSell(): Boolean {
        return this == Sell
    }

    fun isShort() : Boolean {
        return this == Short
    }

    fun reverse() = when(this) {
        Sell, Short -> Buy
        else -> Sell
    }

    fun <R> block(buy: () -> R, sell: () -> R, short: () -> R = sell): R = when(this) {
        Buy -> buy.invoke()
        Sell -> sell.invoke()
        Short -> short.invoke()
    }

    fun <R> block(buy: R, sell: R, short: R = sell) : R = block({ buy }, { sell }, { short })


    companion object {

        @JvmStatic
        fun find(constant: String?) = entries.find { it.constant.equals(constant, true) }
    }
}

