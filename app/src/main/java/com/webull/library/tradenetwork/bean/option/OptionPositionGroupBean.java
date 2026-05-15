package com.webull.library.tradenetwork.bean.option;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Stub: 期权持仓组 Bean（从 AppDev3 同步字段） */
public class OptionPositionGroupBean implements Serializable {
    public String comboId;
    public String comboTickerType;
    public String optionStrategy;
    public String marketValue;
    public String unrealizedProfitLoss;
    public String unrealizedProfitLossRate;
    public String lastPrice;
    public String totalCost;
    public String currency;
    public List<OptionPositionBean> positions;
    public String quantity;
    public String costPrice;
    public BigDecimal dayProfitLoss;
    public String dayProfitLossRate;
}
