package com.webull.commonmodule.trade.bean;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Stub: 通用持仓 Bean（从 AppDev3 同步字段） */
public class CommonPositionBean implements Serializable {
    public String id;
    public String tickerType;
    public String tickerId;
    public String underlyingSymbol;
    public String belongTickerId;
    public String optionType;
    public String optionExpireDate;
    public String optionExercisePrice;
    public String optionSymbol;
    public String optionCategory;
    public String optionContractMultiplier;
    public String optionContractDeliverable;
    public String costPrice;
    public int optionCycle;
    public String lastPrice;
    public String dayProfitLossRate;
    public String expirationType;
    public String interestDetail;
    public String currency;
    public TickerBase ticker;

    private String symbol;
    private String dayProfitLoss;

    public String getSymbol() { return symbol; }
    public void setSymbol(String v) { symbol = v; }
    public String getDayProfitLoss() { return dayProfitLoss; }
    public void setDayProfitLoss(String v) { dayProfitLoss = v; }
}
