package com.webull.library.tradenetwork.bean;

import java.io.Serializable;

/** Stub: 下单请求 Bean（从 AppDev3 同步字段） */
public class PlaceOrder implements Serializable {
    public String action;
    public String quantityType;
    public String quantity;
    public String timeInForce;
    public String orderType;
    public String lmtPrice;
    public String auxPrice;
    public String legOutId;
    public String comboType;

    public String getComboType() { return comboType; }
    public String getLegOutId() { return legOutId; }
}
