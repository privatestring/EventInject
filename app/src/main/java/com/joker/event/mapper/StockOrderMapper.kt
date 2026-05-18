@file:Suppress("unused")

package com.joker.event.mapper

import com.webull.format.utils.parseBigDecimalNullable
import com.webull.library.repository.constant.AtQuantityType
import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import com.webull.library.trade.Constant
import com.webull.library.tradenetwork.bean.PlaceOrder
import com.webull.order.place.dependency.entry.PlaceOrderFormParams
import mapper.Mapper
import mapper.Mapping
import mapper.MappingConfig
import mapper.MappingIgnore
import java.math.BigDecimal

/** 股票Bean对象互转定义 */
@Mapper @MappingConfig interface StockOrderMapper {

    //<editor-fold desc="PlaceOrder 转 PlaceOrderFormParams">
    @Mapping(expression = "java(toFormParamsTargetOrderAction(source))", target = "orderAction")
    @Mapping(expression = "java(toFormParamsTargetQuantityType(source))", target = "quantityType")
    @Mapping(expression = "java(toFormParamsTargetQuantity(source))", target = "quantity")
    @Mapping(expression = "java(toFormParamsTargetTimeInForce(source))", target = "timeInForce")
    @Mapping(expression = "java(toFormParamsTargetLmtPriceAtLimitOrder(source))", target = "lmtPriceAtLimitOrder")
    @Mapping(expression = "java(toFormParamsTargetAuxPriceAtStopOrder(source))", target = "auxPriceAtStopOrder")
    @Mapping(expression = "java(toFormParamsTargetAuxPriceAtStopLimitOrder(source))", target = "auxPriceAtStopLimitOrder")
    @Mapping(expression = "java(toFormParamsTargetLmtPriceAtStopLimitOrder(source))", target = "lmtPriceAtStopLimitOrder")
    fun placeOrderToFormParams(source: PlaceOrder?): PlaceOrderFormParams?

    @MappingIgnore
    fun toFormParamsTargetOrderAction(source: PlaceOrder?) : OrderActionEnum? {
        return OrderActionEnum.find(source?.action)
    }

    @MappingIgnore
    fun toFormParamsTargetQuantityType(source: PlaceOrder?) : AtQuantityType? {
        return AtQuantityType.find(source?.quantityType)
    }

    @MappingIgnore
    fun toFormParamsTargetQuantity(source: PlaceOrder?) : BigDecimal? {
        return source?.quantity.parseBigDecimalNullable()
    }

    @MappingIgnore
    fun toFormParamsTargetTimeInForce(source: PlaceOrder?) : TimeInForceEnum? {
        return TimeInForceEnum.find(source?.timeInForce)
    }

    @MappingIgnore
    fun toFormParamsTargetLmtPriceAtLimitOrder(source: PlaceOrder?) : BigDecimal? {
        return if (Constant.OrderConstant.LMT_TYPE.equals(source?.orderType, true)) {
            source?.lmtPrice.parseBigDecimalNullable()
        } else null
    }

    @MappingIgnore
    fun toFormParamsTargetAuxPriceAtStopOrder(source: PlaceOrder?) : BigDecimal? {
        return if (Constant.OrderConstant.STP_TYPE.equals(source?.orderType, true)) {
            source?.auxPrice.parseBigDecimalNullable()
        } else null
    }

    @MappingIgnore
    fun toFormParamsTargetAuxPriceAtStopLimitOrder(source: PlaceOrder?) : BigDecimal? {
        return if (Constant.OrderConstant.STPLMT_TYPE.equals(source?.orderType, true)) {
            source?.auxPrice.parseBigDecimalNullable()
        } else null
    }

    @MappingIgnore
    fun toFormParamsTargetLmtPriceAtStopLimitOrder(source: PlaceOrder?) : BigDecimal? {
        return if (Constant.OrderConstant.STPLMT_TYPE.equals(source?.orderType, true)) {
            source?.lmtPrice.parseBigDecimalNullable()
        } else null
    }
    //</editor-fold>

    //<editor-fold desc="PlaceOrder 转 PlaceOrderFormParams">
    @Mapping(expression = "java(toPlaceOrderTargetOrderAction(source))", target = "action")
    @Mapping(expression = "java(toPlaceOrderTargetQuantityType(source))", target = "quantityType")
    @Mapping(expression = "java(toPlaceOrderTargetQuantity(source))", target = "quantity")
    @Mapping(expression = "java(toPlaceOrderTargetTimeInForce(source))", target = "timeInForce")
    @Mapping(expression = "java(toPlaceOrderTargetLmtPrice(source))", target = "lmtPrice")
    @Mapping(expression = "java(toPlaceOrderTargetAuxPrice(source))", target = "auxPrice")
    fun formParamsToPlaceOrder(source: PlaceOrderFormParams?): PlaceOrder?

    @MappingIgnore
    fun toPlaceOrderTargetOrderAction(source: PlaceOrderFormParams?) : String? {
        return source?.orderAction?.constant
    }

    @MappingIgnore
    fun toPlaceOrderTargetQuantityType(source: PlaceOrderFormParams?) : String? {
        return source?.quantityType?.constant
    }

    @MappingIgnore
    fun toPlaceOrderTargetQuantity(source: PlaceOrderFormParams?) : String? {
        return source?.quantity?.toPlainString()
    }

    @MappingIgnore
    fun toPlaceOrderTargetTimeInForce(source: PlaceOrderFormParams?) : String? {
        return source?.timeInForce?.constant
    }

    @MappingIgnore
    fun toPlaceOrderTargetLmtPrice(source: PlaceOrderFormParams?) : String? {
        return when {
            Constant.OrderConstant.LMT_TYPE.equals(source?.orderType, true) -> {
                source?.lmtPriceAtLimitOrder?.toPlainString()
            }
            Constant.OrderConstant.STPLMT_TYPE.equals(source?.orderType, true) -> {
                source?.lmtPriceAtStopLimitOrder?.toPlainString()
            }
            else -> null
        }
    }

    @MappingIgnore
    fun toPlaceOrderTargetAuxPrice(source: PlaceOrderFormParams?) : String? {
        return when {
            Constant.OrderConstant.STP_TYPE.equals(source?.orderType, true) -> {
                source?.auxPriceAtStopOrder?.toPlainString()
            }
            Constant.OrderConstant.STPLMT_TYPE.equals(source?.orderType, true) -> {
                source?.auxPriceAtStopLimitOrder?.toPlainString()
            }
            else -> null
        }
    }
    //</editor-fold>

}
