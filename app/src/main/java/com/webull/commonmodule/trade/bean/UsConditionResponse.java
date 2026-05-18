package com.webull.commonmodule.trade.bean;

import androidx.annotation.Nullable;

import com.webull.core.framework.bean.TickerBase;

import java.io.Serializable;

/**
 * 北美条件单对象
 */
public class UsConditionResponse implements Serializable {
        @Nullable public String id;
        @Nullable public TickerBase ticker;
        @Nullable public String field;
        @Nullable public String type;
        @Nullable public String value;
        @Nullable public String operator;
        @Nullable public String unit;
        @Nullable public String benchMark;
}

