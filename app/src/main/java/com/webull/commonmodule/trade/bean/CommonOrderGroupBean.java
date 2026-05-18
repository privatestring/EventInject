package com.webull.commonmodule.trade.bean;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单列表 - 组
 */
public class CommonOrderGroupBean implements Serializable {

    public String orderId;
    /**
     * 组合ID
     */
    public String comboId;

    /**
     * 是否是smart portfolio 再平衡订单
     */
    public String isComboId;
    /**
     * 组合订单类型
     */
    public String comboType;

    /**
     * 类型 股票为EQUITY，期权为OPTION
     */
    @SerializedName(value = "comboTickerType", alternate = {"tickerType", "securityType"})
    public String comboTickerType;

    /**
     * 组合类型 Long Calls，Long Puts等。
     */
    public String optionStrategy;

    /**
     * 是否允许盘前盘后交易
     */
    public boolean outsideRegularTradingHour;

    public String tradingSession;

    public String quantity;

    public String filledQuantity;

    public String action;

    public String timeInForce;

    public String orderType;

    public String lmtPrice;

    public String auxPrice;

    public String avgFilledPrice;

    public boolean canCancel;

    public boolean canModify;

    public String filledAmount;

    public String status;

    @SerializedName(value = "statusStr", alternate = {"statusName"})
    public String statusStr;

    /**
     * 订单数组
     */
    @SerializedName(value = "orders", alternate = {"items", "legs"})
    public List<CommonOrderBean> orders;

    /**
     * leg in还是leg out  leg in为LI，leg out为LO
     */
    public String legInOrLegOut;
    /**
     * leg in后期望的策略
     */
    public String legInStrategy;
    /**
     * 被leg in leg out的持仓ID
     */
    public String legPositionId;

    // rolling 的持仓id, 不为空表示是一个rolling订单
    @Nullable
    public String rollingPositionId;

    /* 所属Wefolio组合名称 **/
    public String comboName;
    /* 所属Wefolio组合介绍 **/
    public String comboIntroduce;
    /* 所属Wefolio组合总已成交金额 **/
    public String filledTotalAmount;
    /* 所属Wefolio组合总订单金额 **/
    public String totalAmount;

    /* 所属smart portfolio组合总订单状态 **/
    public String comboStatusName;

    public boolean isCondition;
    public boolean isConditionActive;

    public ArrayList<StConditionResponse> conditionList; // 条件列表

    public boolean orderTrigger = false; // 基础条件单的条件是否已触发
    /**
     * HK 组合订单类型
     */
    public String superComboType;

    //期权大单是否拆单，false不拆，true拆单
    @Nullable
    public Boolean isNotHeld = null;

    //kalshi相关属性
    public String serialId;

    // 订单创建时间
    public long createTime0;

    /**
     * us股票算法订单新增字段
     */
    public String algoType; // 算法订单类型
    public String algoStartTime;
    public String algoEndTime;
    public String targetVolPercent;
    public String targetVolMaxPercent;
    public String displayAlgoStartTime;
    public String displayAlgoEndTime;

    /**
     * 是否显示AM
     */
    public boolean isShowAmFlag() {
        if (orders == null || orders.isEmpty()) {
            return false;
        }

        for (CommonOrderBean orderBean : orders) {
            if (orderBean == null) {
                return false;
            }

        }
        return true;
    }

    /** 获取止损价 */
    public @Nullable String getAuxPrice() {
        if ("STP".equals(orderType) || "STP LMT".equals(orderType)) {
            if (conditionList != null) { // 止损单
                for (StConditionResponse conditionData : conditionList) {
                    if ("BASE".equals(conditionData.getConditionType()) && "VALUE".equals(conditionData.getCompareFieldType())) { // 并且找到了基础条件
                        if (!TextUtils.isEmpty(conditionData.getCompareValue())) return conditionData.getCompareValue();
                    }
                }
            }
        }
        // 找不到用原始值兜底
        return auxPrice;
    }

    public void setAuxPrice(@Nullable String auxPrice) {
        if ("STP".equals(orderType) || "STP LMT".equals(orderType)) {
            if (conditionList != null) { // 止损单
                for (StConditionResponse conditionData : conditionList) {
                    if ("BASE".equals(conditionData.getConditionType()) && "VALUE".equals(conditionData.getCompareFieldType())) { // 并且找到了基础条件
                        conditionData.setCompareValue(auxPrice);
                    }
                }
            }
        }
        this.auxPrice = auxPrice;
    }

    @Override
    public String toString() {

        return "CommonOrderGroupBean{" +
                "comboId='" + comboId + '\'' +
                ", comboType='" + comboType + '\'' +
                ", comboTickerType='" + comboTickerType + '\'' +
                ", optionStrategy='" + optionStrategy + '\'' +
                ", outsideRegularTradingHour=" + outsideRegularTradingHour +
                ", quantity='" + quantity + '\'' +
                ", filledQuantity='" + filledQuantity + '\'' +
                ", action='" + action + '\'' +
                ", timeInForce='" + timeInForce + '\'' +
                ", orderType='" + orderType + '\'' +
                ", lmtPrice='" + lmtPrice + '\'' +
                ", auxPrice='" + auxPrice + '\'' +
                ", avgFilledPrice='" + avgFilledPrice + '\'' +
                ", canCancel=" + canCancel +
                ", statusStr='" + statusStr + '\'' +
                ", orders=" + orders +
                '}';
    }
}
