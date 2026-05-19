package com.webull.core.framework.bean;

import androidx.annotation.Nullable;

import java.io.Serializable;

import wb.bean.AutoUpdate;
import wb.bean.AutoUpdateCheck;

/** Stub: 标的档案信息（从 AppDev3 同步字段） */
@AutoUpdate
public class TickerBase implements Serializable {
    public static final int TICKER_TYPE_OPTION = 2;

    public String tickerId = "";
    public int regionId;
    public int type;
    public String symbol;
    public String disSymbol;
    public String unSymbol;
    public String name;
    public String exchangeCode;
    public String derivativeId;
    public String strikePrice;
    public String expireDate;
    public String direction;
    public Integer weekly;
    public int quoteMultiplier;
    public String optionSymbol;
    public Integer subType;
    public String belongTickerId;
    public String quoteLotSize;
    public String currency;
    public String lastPrice;
    public String expirationType;
    public String changeRatio;
    public int isStdSettle;

    @AutoUpdateCheck(condition = "{from}.listStatusInteger != null")
    private Integer listStatus;

    public int getListStatus() {
        return listStatus == null ? 0 : listStatus;
    }

    @Nullable
    public Integer getListStatusInteger() {
        return listStatus;
    }

    public void setListStatus(int listStatus) {
        this.listStatus = listStatus;
    }


    public String getTickerId() { return tickerId; }
    public void setTickerId(String v) { tickerId = v; }
    public String getDisSymbol() { return disSymbol; }
    public void setDisSymbol(String v) { disSymbol = v; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String v) { symbol = v; }
    public void setUnSymbol(String v) { unSymbol = v; }
    public void setStrikePrice(String v) { strikePrice = v; }
    public void setType(int v) { type = v; }
    public void setExpireDate(String v) { expireDate = v; }
    public void setDirection(String v) { direction = v; }
    public void setWeekly(Integer v) { weekly = v; }
    public void setQuoteMultiplier(int v) { quoteMultiplier = v; }
    public void setOptionSymbol(String v) { optionSymbol = v; }
    public void setSubType(Integer v) { subType = v; }
    public void setDerivativeId(String v) { derivativeId = v; }
    public void setBelongTickerId(String v) { belongTickerId = v; }
    public void setQuoteLotSize(String v) { quoteLotSize = v; }
    public int getRegionId() { return regionId; }
    public void setCurrency(String v) { currency = v; }
    public void setLastPrice(String v) { lastPrice = v; }
    public void setExpirationType(String v) { expirationType = v; }
    public void setChangeRatio(String v) { changeRatio = v; }
    public void setIsStdSettle(int v) { isStdSettle = v; }
    public void setExchangeCode(String v) { exchangeCode = v; }
    public void setRegionId(int v) { regionId = v; }
    public void setStockSymbol(String v) { /* TickerBase 没有此字段，由子类处理 */ }
    public void setTickerName(String v) { name = v; }
    public void setBelongTickerPrice(String v) { /* stub */ }
    public void setPositionCostPrice(String v) { /* stub */ }
}
