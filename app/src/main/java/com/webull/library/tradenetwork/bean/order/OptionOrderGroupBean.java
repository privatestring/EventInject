package com.webull.library.tradenetwork.bean.order;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.util.List;

/** Stub: 期权订单组 Bean（从 AppDev3 同步字段） */
public class OptionOrderGroupBean implements Serializable {
    public String comboId;
    public String comboTickerType;
    public String optionStrategy;
    public String action;
    public String auxPrice;
    public String lmtPrice;
    public String quantity;
    public String timeInForce;
    public String trailingStopStep;
    public String trailingType;
    public String orderType;
    public String comboType;
    public TickerBase ticker;
    public List<OptionOrderBean> orders;
}
