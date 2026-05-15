package com.webull.commonmodule.networkinterface.quoteapi.beans.option;

import java.io.Serializable;

/**
 * 期权行情 Bean（精简版，仅保留 Mapper 使用的字段）
 */
public class TickerOptionBean implements Serializable {

    public static final int SUB_TYPE_INDEX_OPTION_CALL = 1;
    public static final int SUB_TYPE_INDEX_OPTION_PUT = 2;
    public static final int SUB_TYPE_FUTURES_OPTION_CALL = 3;
    public static final int SUB_TYPE_FUTURES_OPTION_PUT = 4;

    private String tickerId;
    private String belongTickerId;
    private String expireDate;
    private String strikePrice;
    private String direction;
    private String symbol;
    private String unSymbol;
    private String quoteMultiplier;
    private String quoteLotSize;
    private String weekly;
    private String regionId;
    private String exchangeCode;
    private int subType;
    private String positionCostPrice;
    private String stockSymbol;
    private String currency;
    private String lastPrice;
    private String optionSymbol;
    private String expirationType;
    private String changeRatio;
    private int isStdSettle;

    public String getTickerId() { return tickerId; }
    public void setTickerId(String v) { tickerId = v; }
    public String getBelongTickerId() { return belongTickerId; }
    public void setBelongTickerId(String v) { belongTickerId = v; }
    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String v) { expireDate = v; }
    public String getStrikePrice() { return strikePrice; }
    public void setStrikePrice(String v) { strikePrice = v; }
    public String getDirection() { return direction; }
    public void setDirection(String v) { direction = v; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String v) { symbol = v; }
    public String getUnSymbol() { return unSymbol; }
    public void setUnSymbol(String v) { unSymbol = v; }
    public String getQuoteMultiplier() { return quoteMultiplier; }
    public void setQuoteMultiplier(String v) { quoteMultiplier = v; }
    public String getQuoteLotSize() { return quoteLotSize; }
    public void setQuoteLotSize(String v) { quoteLotSize = v; }
    public String getWeekly() { return weekly; }
    public void setWeekly(String v) { weekly = v; }
    public String getRegionId() { return regionId; }
    public void setRegionId(String v) { regionId = v; }
    public String getExchangeCode() { return exchangeCode; }
    public void setExchangeCode(String v) { exchangeCode = v; }
    public int getSubType() { return subType; }
    public void setSubType(int v) { subType = v; }
    public String getPositionCostPrice() { return positionCostPrice; }
    public void setPositionCostPrice(String v) { positionCostPrice = v; }
    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String v) { stockSymbol = v; }
    public String getCurrency() { return currency; }
    public void setCurrency(String v) { currency = v; }
    public String getLastPrice() { return lastPrice; }
    public void setLastPrice(String v) { lastPrice = v; }
    public String getOptionSymbol() { return optionSymbol; }
    public void setOptionSymbol(String v) { optionSymbol = v; }
    public String getExpirationType() { return expirationType; }
    public void setExpirationType(String v) { expirationType = v; }
    public String getChangeRatio() { return changeRatio; }
    public void setChangeRatio(String v) { changeRatio = v; }
    public int getIsStdSettle() { return isStdSettle; }
    public void setIsStdSettle(int v) { isStdSettle = v; }
}
