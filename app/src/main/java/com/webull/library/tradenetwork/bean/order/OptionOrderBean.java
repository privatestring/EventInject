package com.webull.library.tradenetwork.bean.order;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Stub: 期权订单 Bean（从 AppDev3 同步字段） */
public class OptionOrderBean implements Serializable {
    public String orderId;
    public String mlegId;
    public String tickerType;
    public String action;
    public String tickerId;
    public String belongTickerId;
    public String optionType;
    public String optionExpireDate;
    public String optionExercisePrice;
    public String quantity;
    public String filledQuantity;
    public String status;
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
    public Integer subType;
    public TickerBase ticker;
    public String orderType;
    public String lmtPrice;
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

    private String auxPrice;

    public String getUnSymbol() { return underlyingSymbol; }
    public String getQuantity() { return quantity; }
    public String getOrderType() { return orderType; }
    public String getComboType() { return comboType; }
    public String getAuxPrice() { return auxPrice; }
    public void setAuxPrice(String v) { auxPrice = v; }
    public String getBelongTickerId() { return belongTickerId; }
}
