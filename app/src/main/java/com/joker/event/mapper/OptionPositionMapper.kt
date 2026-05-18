package com.joker.event.mapper

import com.webull.commonmodule.trade.bean.CommonPositionBean
import com.webull.commonmodule.trade.bean.CommonPositionGroupBean
import com.webull.core.framework.Constants
import com.webull.core.framework.bean.TickerBase
import com.webull.format.utils.parseBigDecimal
import com.webull.format.utils.parseInt
import com.webull.library.tradenetwork.bean.option.OptionPositionBean
import com.webull.library.tradenetwork.bean.option.OptionPositionGroupBean
import mapper.Mapper
import mapper.Mapping
import mapper.MappingConfig
import mapper.MappingIgnore

@Mapper
/**
 * 期权持仓相关，实体类更新/互转
 **/
@MappingConfig()
interface OptionPositionMapper {

    @Mapping(source = "ticker", target = "ticker", ignore = true)
    fun toOptionPositionBean(
        source: CommonPositionBean?):OptionPositionBean?

    @Mapping(expression = "java(source.dayProfitLossRate != null ? source.dayProfitLossRate.toPlainString() : null)", target = "dayProfitLossRate")
    fun toOptionPositionGroupBean(
        source: CommonPositionGroupBean?):OptionPositionGroupBean

    @Mapping(source = "belongTickerId", target = "tickerId")
    @Mapping(source = "tickerId", target = "derivativeId")
    @Mapping(source = "underlyingSymbol", target = "symbol")
    @Mapping(source = "underlyingSymbol", target = "unSymbol")
    @Mapping(source = "optionExercisePrice", target = "strikePrice")
    @Mapping(expression = "java(TickerBase.TICKER_TYPE_OPTION)", target = "type")
    @Mapping(source = "optionExpireDate", target = "expireDate")
    @Mapping(source = "optionType", target = "direction")
    @Mapping(expression = "java(getWeekly(source.optionCycle))", target = "weekly")
    @Mapping(
        expression = "java(formatMultiple(source.optionContractMultiplier))",
        target = "quoteMultiplier"
    )
    @Mapping(source = "belongTickerId", target = "belongTickerId", ignore = true)
    fun toOptionTickerBase(source: OptionPositionBean?): TickerBase?

    @MappingIgnore
    fun formatMultiple(optionContractMultiplier: String?): Int? {
        if (optionContractMultiplier == null) return 100
        return optionContractMultiplier.parseBigDecimal(replace = "100")?.parseInt()
    }

    @MappingIgnore
    fun getWeekly(optionCycle: Int?): Int? {
        //是call还是put
        if (optionCycle == null) return -1
        return if (optionCycle == Constants.OptionConstant.OptionCycle.WEEKLY || Constants.OptionConstant.OptionCycle.END_OF_MONTH == optionCycle) {
            1
        } else {
            -1
        }
    }
}