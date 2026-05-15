package com.webull.commonmodule.trade.bean

import com.webull.core.framework.bean.TickerBase

/**
 * 其他券商条件单对象
 */
class StConditionResponse :java.io.Serializable{
    val id: String? = null // 条件Id
    val tickerId:String? = null //条件标的id
    val ticker: TickerBase? = null //条件标的
    var compareField:String? = null //条件因子 ： PRICE // 最新市价 PRICE_ASK // 卖一价 PRICE_BID // 买一价 VOLUME // 成交量
    var compareFieldType:String? = null //因子类别，VALUE: 值 ，PERCENT：百分比
    val compareType:String? = null //比较类型
    var compareValue:String? = null //值  （跟踪止损限价单 下该值为跟踪价差值   在触及限价单、触及市价单下该值为触发价）
    val conditionType:String? = null //base：基础条件， append ：附加条件
    val operator:String? = null //操作符
}