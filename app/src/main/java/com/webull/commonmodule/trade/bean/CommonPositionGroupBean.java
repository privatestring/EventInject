package com.webull.commonmodule.trade.bean;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Stub: 通用持仓组 Bean（从 AppDev3 同步字段） */
public class CommonPositionGroupBean implements Serializable {
    public String comboId;
    public String comboTickerType;
    public String optionStrategy;
    public String marketValue;
    public String unrealizedProfitLoss;
    public String unrealizedProfitLossRate;
    public String lastPrice;
    public String totalCost;
    public String currency;
    public List<CommonPositionBean> positions;
    public String quantity;
    public String costPrice;
    @Nullable
    public BigDecimal dayProfitLoss;
    @Nullable
    public BigDecimal dayProfitLossRate;
}
