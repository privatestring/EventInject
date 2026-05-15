package com.webull.library.tradenetwork.bean.option;

import com.webull.core.framework.bean.TickerRealtimeV2;
import java.io.Serializable;
import java.util.List;

/** Stub: 期权持仓 Bean（从 AppDev3 同步字段） */
public class OptionPositionBean implements Serializable {
    public String id;
    public String tickerType;
    public String tickerId;
    public String underlyingSymbol;
    public String belongTickerId;
    public String optionType;
    public String optionExpireDate;
    public String optionExercisePrice;
    public String symbol;
    public String optionSymbol;
    public String optionCategory;
    public String optionContractMultiplier;
    public String optionContractDeliverable;
    public String costPrice;
    public int optionCycle;
    public String lastPrice;
    public String dayProfitLoss;
    public String dayProfitLossRate;
    public String expirationType;
    public String interestDetail;
    public String currency;
    public int subType;
    public TickerRealtimeV2 ticker;

    public String getBelongTickerId() { return belongTickerId; }
    public void setCurrency(String v) { currency = v; }
}
