package com.webull.commonmodule.trade.bean;

import java.io.Serializable;
import java.util.List;

/** Stub: 通用订单组 Bean（从 AppDev3 同步字段） */
public class CommonOrderGroupBean implements Serializable {
    public String comboId;
    public String comboTickerType;
    public String optionStrategy;
    public List<CommonOrderBean> orders;
}
