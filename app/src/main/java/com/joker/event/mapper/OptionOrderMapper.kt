package com.joker.event.mapper

import com.webull.commonmodule.trade.bean.CommonOrderBean
import com.webull.commonmodule.trade.bean.CommonOrderGroupBean
import com.webull.core.framework.bean.TickerBase
import com.webull.library.tradenetwork.bean.order.OptionOrderBean
import com.webull.library.tradenetwork.bean.order.OptionOrderGroupBean
import com.webull.order.place.dependency.entry.SnapshotFormParams
import mapper.Mapper
import mapper.Mapping
import mapper.MappingConfig
import mapper.MappingIgnore

@Mapper
@MappingConfig()
/**
 * 期权订单相关，实体类更新/互转
 **/
interface OptionOrderMapper {

    @Mapping(source = "totalQuantity", target = "quantity")
    @Mapping(source = "statusCode", target = "status")
    @Mapping(expression = "java(getBelongTickerId(source))", target = "belongTickerId")
    fun toOptionOrderBean(source: CommonOrderBean?): OptionOrderBean?

    fun toOptionOrderBeanList(source: List<CommonOrderBean>?): ArrayList<OptionOrderBean>?

    @Mapping(expression = "java(getTicker(source))", target = "ticker")
    fun toOptionOrderGroupBean(source: CommonOrderGroupBean?): OptionOrderGroupBean?

    @Mapping(expression = "java(getOrderAction(source))", target = "action")
    @Mapping(expression = "java(getAuxPrice(source))", target = "auxPrice")
    @Mapping(expression = "java(getLmtPrice(source))", target = "lmtPrice")
    @Mapping(expression = "java(getQuantity(source))", target = "quantity")
    @Mapping(expression = "java(getTimeInForce(source))", target = "timeInForce")
    @Mapping(source = "trailingStopStep", target = "trailingStopStep", ignore = true)
    @Mapping(source = "trailingType", target = "trailingType", ignore = true)
    fun toOptionOrderGroupBeanNew(source: SnapshotFormParams?): OptionOrderGroupBean?

    @Mapping(source = "quantity", target = "totalQuantity")
    @Mapping(source = "status", target = "statusCode")
    fun toCommonOrderBean(source: OptionOrderBean?): CommonOrderBean?

    fun toCommonOrderGroupBean(source: OptionOrderGroupBean?): CommonOrderGroupBean?


    @MappingIgnore
    fun getBelongTickerId(source: CommonOrderBean?):String?{
        try {
            if (source?.belongTickerId.isNullOrEmpty().not()) return source?.belongTickerId
            return source?.ticker?.getTickerId()
        } catch (_: Exception) {
        }
        return null
    }

    @MappingIgnore
    fun getTicker(source: CommonOrderGroupBean?): TickerBase?{
        try {
            return source?.orders?.firstOrNull()?.ticker
        } catch (_: Exception) {
        }
        return null
    }

    @MappingIgnore
    fun getOrderAction(source: SnapshotFormParams?): String?{
        return source?.orderAction?.constant
    }

    @MappingIgnore
    fun getAuxPrice(source: SnapshotFormParams?): String?{
        return source?.auxPriceAtStopOrder?.toPlainString()
    }

    @MappingIgnore
    fun getLmtPrice(source: SnapshotFormParams?): String?{
        return source?.lmtPriceAtLimitOrder?.toPlainString()
    }
    @MappingIgnore
    fun getQuantity(source: SnapshotFormParams?): String?{
        return source?.quantity?.toPlainString()
    }
    @MappingIgnore
    fun getTimeInForce(source: SnapshotFormParams?): String?{
        return source?.timeInForce?.constant
    }
}