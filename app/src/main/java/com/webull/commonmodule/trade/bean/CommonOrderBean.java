package com.webull.commonmodule.trade.bean;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Stub: 通用订单 Bean（从 AppDev3 同步字段） */
public class CommonOrderBean implements Serializable {
    public String orderId;
    public String mlegId;
    public String tickerType;
    public String action;
    public String tickerId;
    public String belongTickerId;
    public String optionType;
    public String optionExpireDate;
    public String optionExercisePrice;
    public String totalQuantity;
    public String filledQuantity;
    public String statusCode;
    public String statusStr;
    public String symbol;
    public String underlyingSymbol;
    public String optionCategory;
    public String optionContractMultiplier;
    public String optionContractDeliverable;
    public String avgFilledPrice;
    public String currency;
    public int optionCycle;
    public String filledTime;
    public String expireDate;
    public String expireTime;
    public String expirationType;
    public String subType;
    public TickerBase ticker;
    public String orderType;
    public String lmtPrice;
    public String auxPrice;
    public String timeInForce;
    public String comboType;
    public String comboTickerType;
    public boolean outsideRegularTradingHour;
    public boolean canCancel;
    public boolean canModify;
    public String filledAmount;
    public String legInOrLegOut;
    public String legInStrategy;
    public String legPositionId;
    public String rollingPositionId;
    public boolean isCondition;
    public boolean isConditionActive;
    public ArrayList conditionList;
    public boolean orderTrigger;
    public String isNotHeld;
    public String regionId;
    public Integer optionCycleInt;

    public String getUnSymbol() { return underlyingSymbol; }
}
