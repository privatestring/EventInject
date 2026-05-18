package com.joker.event.mapper

import com.webull.commonmodule.networkinterface.quoteapi.beans.option.TickerOptionBean
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.TickerOptionBean.SUB_TYPE_FUTURES_OPTION_CALL
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.TickerOptionBean.SUB_TYPE_FUTURES_OPTION_PUT
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.TickerOptionBean.SUB_TYPE_INDEX_OPTION_CALL
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.TickerOptionBean.SUB_TYPE_INDEX_OPTION_PUT
import com.webull.commonmodule.trade.bean.CommonOrderBean
import com.webull.commonmodule.trade.bean.CommonPositionBean
import com.webull.core.framework.Constants
import com.webull.core.framework.bean.TickerBase
import com.webull.core.utils.TickerUtils
import com.webull.format.utils.orZero
import com.webull.library.tradenetwork.bean.option.OptionPositionBean
import com.webull.library.tradenetwork.bean.order.OptionOrderBean
import mapper.Mapper
import mapper.Mapping
import mapper.MappingConfig
import mapper.MappingIgnore

@Mapper
@MappingConfig()
/**
 * 期权行情相关，实体类更新/互转
 **/
interface TickerOptionBeanMapper {

    @Mapping(source = "optionExpireDate", target = "expireDate")
    @Mapping(source = "optionExercisePrice", target = "strikePrice")
    @Mapping(source = "optionType", target = "direction")
//    @Mapping(source = "underlyingSymbol", target = "symbol")
    @Mapping(source = "symbol", target = "unSymbol")
    @Mapping(source = "optionContractMultiplier", target = "quoteMultiplier")
    @Mapping(source = "optionContractDeliverable", target = "quoteLotSize")
    @Mapping(expression = "java(getWeeklyStr(source.optionCycle))", target = "weekly")
    @Mapping(expression = "java(getCallOrPut(source.optionType))", target = "direction")
    @Mapping(source = "costPrice", target = "positionCostPrice")
    @Mapping(expression = "java(source.ticker != null ? String.valueOf(source.ticker.getRegionId()) : null)", target = "regionId")
    @Mapping(expression = "java(getExchangeCode(source))", target = "exchangeCode")
    @Mapping(expression = "java(getSymbol(source))", target = "symbol")
    fun toTickerOptionBean(source: OptionPositionBean?): TickerOptionBean?

    @Mapping(expression = "java(source.ticker != null ? source.ticker.getTickerId() : \"\")", target = "belongTickerId")
    @Mapping(source = "optionExpireDate", target = "expireDate")
    @Mapping(source = "optionExercisePrice", target = "strikePrice")
    @Mapping(source = "optionType", target = "direction")
    @Mapping(expression = "java(source.ticker != null ? source.ticker.getDisSymbol() : \"\")", target = "symbol")
    @Mapping(source = "symbol", target = "unSymbol")
    @Mapping(source = "optionContractDeliverable", target = "quoteLotSize")
    @Mapping(source = "optionContractMultiplier", target = "quoteMultiplier")
    @Mapping(expression = "java(getWeeklyStr(source.optionCycle))", target = "weekly")
    fun toTickerOptionBean(source: CommonPositionBean?): TickerOptionBean?

    @Mapping(source = "optionExpireDate", target = "expireDate")
    @Mapping(source = "optionExercisePrice", target = "strikePrice")
    @Mapping(source = "optionType", target = "direction")
    @Mapping(expression = "java(source.getUnSymbol())", target = "unSymbol")
    @Mapping(source = "optionContractMultiplier", target = "quoteMultiplier")
    @Mapping(source = "optionContractDeliverable", target = "quoteLotSize")
    @Mapping(expression = "java(getWeeklyStr(source.optionCycle))", target = "weekly")
    @Mapping(expression = "java(getCallOrPut(source.optionType))", target = "direction")
    @Mapping(expression = "java(getRegionId(source.ticker,target))", target = "regionId")
    @Mapping(expression = "java(getSubType(source.ticker,source.optionType))", target = "subType")
    fun toTickerOptionBean(source: OptionOrderBean?): TickerOptionBean?

    @Mapping(expression = "source.ticker != null ? source.ticker.getTickerId() : source.belongTickerId", target = "belongTickerId")
    @Mapping(expression = "source.ticker != null ? source.ticker.getDisSymbol() : source.symbol", target = "stockSymbol")
    @Mapping(expression = "java(getCallOrPut(source.optionType))", target = "direction")
    @Mapping(source = "optionContractMultiplier", target = "quoteMultiplier")
    @Mapping(source = "optionExercisePrice", target = "strikePrice")
    @Mapping(source = "optionExpireDate", target = "expireDate")
    @Mapping(expression = "java(source.getUnSymbol())", target = "unSymbol")
    @Mapping(expression = "java(getSubType(source.subType))", target = "subType")
    @Mapping(source = "optionContractDeliverable", target = "quoteLotSize")
    @Mapping(expression = "java(getWeeklyStr(source.optionCycle))", target = "weekly")
    fun toTickerOptionBean(source: CommonOrderBean?): TickerOptionBean?

    @MappingIgnore
    fun getRegionId(ticker: TickerBase?, target:TickerOptionBean?): String? {
        if (ticker == null || ticker.regionId == 0){
            return target?.regionId
        }
        return if (target == null || target.regionId.isNullOrEmpty()){ //期权传正股的region id
            ticker.regionId.toString()
        } else {
            target.regionId
        }
    }

    @MappingIgnore
    fun getSymbol(source: OptionPositionBean?): String? {
        if (source == null) return null
        if (source.underlyingSymbol.isNullOrEmpty().not()) return source.underlyingSymbol
        return if (source.ticker !=null){
            source.ticker.getDisSymbol()
        } else{
            null
        }
    }

    @MappingIgnore
    fun getCallOrPut(optionType: String?): String? {
        //是call还是put
        return if (Constants.OptionConstant.CALL.equals(optionType, ignoreCase = true)) {
            Constants.OptionConstant.CALL
        } else {
            Constants.OptionConstant.PUT
        }
    }

    @MappingIgnore
    fun getWeeklyStr(optionCycle: Int?): String? {
        //是call还是put
        if (optionCycle == null) return "0"
        return if (optionCycle == Constants.OptionConstant.OptionCycle.WEEKLY || Constants.OptionConstant.OptionCycle.END_OF_MONTH == optionCycle) {
            "1"
        } else {
            "0"
        }
    }

    @MappingIgnore
    fun getSubType(subType:String?):Int{
        runCatching {
            return subType?.toInt().orZero()
        }
        return 0
    }

    @MappingIgnore
    fun getSubType(realtime: TickerBase?,optionType:String?): Int {
        //是call还是put
        if (realtime == null) return 0
        return if (realtime.regionId == Constants.RegionConstants.REGION_HK
            && realtime.type == TickerUtils.TICKER_TYPE_INDEX
        ) {
            return if (Constants.OptionConstant.CALL.equals(
                    optionType,
                    ignoreCase = true
                )
            ) SUB_TYPE_INDEX_OPTION_CALL else SUB_TYPE_INDEX_OPTION_PUT
        } else if (realtime.regionId == Constants.RegionConstants.REGION_HK
            && realtime.type == TickerUtils.TICKER_TYPE_FUTURES
        ) {
            return if (Constants.OptionConstant.CALL.equals(
                    optionType,
                    ignoreCase = true
                )
            ) SUB_TYPE_FUTURES_OPTION_CALL else SUB_TYPE_FUTURES_OPTION_PUT
        } else 0
    }

    @MappingIgnore
    fun getExchangeCode(source: OptionPositionBean?): String? {
        //是call还是put
        if (source == null || source.ticker == null ) return null
        return if (source.ticker.regionId == Constants.RegionConstants.REGION_HK
            && source.ticker.type == TickerUtils.TICKER_TYPE_FUTURES) {
            source.ticker.exchangeCode
        } else TickerUtils.OPTION_EXCHANGE_CODE
    }
}