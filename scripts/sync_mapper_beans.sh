#!/bin/bash
# ============================================================
# 从 AppDev3 项目同步 Mapper 引用的 bean 类到测试项目
# 提取 Mapper 实际使用的属性和方法
#
# 使用方式：
#   cd /Users/joker/webull/webull/inject/EventInject
#   bash scripts/sync_mapper_beans.sh
# ============================================================

set -e

APP_SRC="/Users/joker/webull/webull/inject/EventInject/app/src/main/java"
APPDEV3="/Users/joker/webull/git/AppDev3"

echo "=== 从 AppDev3 提取 bean 类字段信息并生成 stub ==="

# ---- OptionOrderBean (Java) ----
# 源文件: tradesdk/TradeCore/src/main/java/com/webull/library/tradenetwork/bean/order/OptionOrderBean.java
echo "  提取 OptionOrderBean..."
DIR="$APP_SRC/com/webull/library/tradenetwork/bean/order"
mkdir -p "$DIR"

# 提取所有 public 字段和方法
cat > "$DIR/OptionOrderBean.java" << 'STUB'
package com.webull.library.tradenetwork.bean.order;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Stub: 期权订单 Bean（从 AppDev3 同步字段） */
public class OptionOrderBean implements Serializable {
    public String orderId;
    public String mlegId;
    public String tickerType;
    public String action;
    public String tickerId;
    public String belongTickerId;
    public String optionType;
    public String optionExpireDate;
    public String optionExercisePrice;
    public String quantity;
    public String filledQuantity;
    public String status;
    public String statusStr;
    public String symbol;
    public String underlyingSymbol;
    public String optionCategory;
    public String optionContractMultiplier;
    public String optionContractDeliverable;
    public String avgFilledPrice;
    public String currency;
    public int optionCycle;
    public String filledTime;
    public String expireDate;
    public String expireTime;
    public String expirationType;
    public Integer subType;
    public TickerBase ticker;
    public String orderType;
    public String lmtPrice;
    public String timeInForce;
    public String comboType;
    public String comboTickerType;
    public boolean outsideRegularTradingHour;
    public boolean canCancel;
    public boolean canModify;
    public String filledAmount;
    public String legInOrLegOut;
    public String legInStrategy;
    public String legPositionId;
    public String rollingPositionId;
    public boolean isCondition;
    public boolean isConditionActive;
    public ArrayList conditionList;
    public boolean orderTrigger;
    public String isNotHeld;

    private String auxPrice;

    public String getUnSymbol() { return underlyingSymbol; }
    public String getQuantity() { return quantity; }
    public String getOrderType() { return orderType; }
    public String getComboType() { return comboType; }
    public String getAuxPrice() { return auxPrice; }
    public void setAuxPrice(String v) { auxPrice = v; }
    public String getBelongTickerId() { return belongTickerId; }
}
STUB
echo "  ✓ OptionOrderBean.java"

# ---- OptionOrderGroupBean (Java) ----
cat > "$DIR/OptionOrderGroupBean.java" << 'STUB'
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
STUB
echo "  ✓ OptionOrderGroupBean.java"

# ---- CommonOrderBean (Java) ----
DIR="$APP_SRC/com/webull/commonmodule/trade/bean"
mkdir -p "$DIR"
cat > "$DIR/CommonOrderBean.java" << 'STUB'
package com.webull.commonmodule.trade.bean;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Stub: 通用订单 Bean（从 AppDev3 同步字段） */
public class CommonOrderBean implements Serializable {
    public String orderId;
    public String mlegId;
    public String tickerType;
    public String action;
    public String tickerId;
    public String belongTickerId;
    public String optionType;
    public String optionExpireDate;
    public String optionExercisePrice;
    public String totalQuantity;
    public String filledQuantity;
    public String statusCode;
    public String statusStr;
    public String symbol;
    public String underlyingSymbol;
    public String optionCategory;
    public String optionContractMultiplier;
    public String optionContractDeliverable;
    public String avgFilledPrice;
    public String currency;
    public int optionCycle;
    public String filledTime;
    public String expireDate;
    public String expireTime;
    public String expirationType;
    public String subType;
    public TickerBase ticker;
    public String orderType;
    public String lmtPrice;
    public String auxPrice;
    public String timeInForce;
    public String comboType;
    public String comboTickerType;
    public boolean outsideRegularTradingHour;
    public boolean canCancel;
    public boolean canModify;
    public String filledAmount;
    public String legInOrLegOut;
    public String legInStrategy;
    public String legPositionId;
    public String rollingPositionId;
    public boolean isCondition;
    public boolean isConditionActive;
    public ArrayList conditionList;
    public boolean orderTrigger;
    public String isNotHeld;
    public String regionId;
    public Integer optionCycleInt;

    public String getUnSymbol() { return underlyingSymbol; }
}
STUB
echo "  ✓ CommonOrderBean.java"

# ---- CommonOrderGroupBean (Java) ----
cat > "$DIR/CommonOrderGroupBean.java" << 'STUB'
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
STUB
echo "  ✓ CommonOrderGroupBean.java"

# ---- CommonPositionBean (Java) ----
cat > "$DIR/CommonPositionBean.java" << 'STUB'
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
STUB
echo "  ✓ CommonPositionBean.java"

# ---- CommonPositionGroupBean (Java) ----
cat > "$DIR/CommonPositionGroupBean.java" << 'STUB'
package com.webull.commonmodule.trade.bean;

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
    public String dayProfitLoss;
    public BigDecimal dayProfitLossRate;
}
STUB
echo "  ✓ CommonPositionGroupBean.java"

# ---- OptionPositionBean (Java) ----
DIR="$APP_SRC/com/webull/library/tradenetwork/bean/option"
mkdir -p "$DIR"
cat > "$DIR/OptionPositionBean.java" << 'STUB'
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
STUB
echo "  ✓ OptionPositionBean.java"

# ---- OptionPositionGroupBean (Java) ----
cat > "$DIR/OptionPositionGroupBean.java" << 'STUB'
package com.webull.library.tradenetwork.bean.option;

import java.io.Serializable;
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
    public String dayProfitLoss;
    public String dayProfitLossRate;
}
STUB
echo "  ✓ OptionPositionGroupBean.java"

# ---- PlaceOrder (Java) ----
DIR="$APP_SRC/com/webull/library/tradenetwork/bean"
mkdir -p "$DIR"
cat > "$DIR/PlaceOrder.java" << 'STUB'
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
STUB
echo "  ✓ PlaceOrder.java"

# ---- PlaceOrderFormParams (Kotlin) ----
DIR="$APP_SRC/com/webull/order/place/dependency/entry"
mkdir -p "$DIR"
cat > "$DIR/PlaceOrderFormParams.kt" << 'STUB'
package com.webull.order.place.dependency.entry

import com.webull.library.repository.constant.AtQuantityType
import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import java.math.BigDecimal

/** Stub: 下单表单参数（从 AppDev3 同步字段） */
class PlaceOrderFormParams {
    var orderAction: OrderActionEnum? = null
    var quantityType: AtQuantityType? = null
    var quantity: BigDecimal? = null
    var timeInForce: TimeInForceEnum? = null
    var orderType: String? = null
    var lmtPriceAtLimitOrder: BigDecimal? = null
    var auxPriceAtStopOrder: BigDecimal? = null
    var auxPriceAtStopLimitOrder: BigDecimal? = null
    var lmtPriceAtStopLimitOrder: BigDecimal? = null
    var legOutId: String? = null
    var comboType: String? = null
    var isLegOut: Boolean? = null

    fun getComboType(): String? = comboType
    fun getLegOutId(): String? = legOutId
    fun setLegOutId(v: String?) { legOutId = v }
    fun setComboType(v: String?) { comboType = v }
}
STUB
echo "  ✓ PlaceOrderFormParams.kt"

# ---- SnapshotFormParams (Kotlin) ----
cat > "$DIR/SnapshotFormParams.kt" << 'STUB'
package com.webull.order.place.dependency.entry

import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import java.math.BigDecimal

/** Stub: 快照表单参数（从 AppDev3 同步字段） */
class SnapshotFormParams {
    var orderAction: OrderActionEnum? = null
    var quantity: BigDecimal? = null
    var timeInForce: TimeInForceEnum? = null
    var lmtPriceAtLimitOrder: BigDecimal? = null
    var auxPriceAtStopOrder: BigDecimal? = null
    var trailingStopStep: String? = null
    var trailingType: String? = null
    var orderType: String? = null
    var comboType: String? = null
}
STUB
echo "  ✓ SnapshotFormParams.kt"

# ---- TickerOptionBean (Kotlin) ----
DIR="$APP_SRC/com/webull/commonmodule/networkinterface/quoteapi/beans/option"
mkdir -p "$DIR"
cat > "$DIR/TickerOptionBean.kt" << 'STUB'
package com.webull.commonmodule.networkinterface.quoteapi.beans.option

import java.util.Date

/** Stub: 期权行情 Bean（从 AppDev3 同步字段） */
class TickerOptionBean {
    var tickerId: String? = null
    var belongTickerId: String? = null
    var expireDate: String? = null
    var strikePrice: String? = null
    var direction: String? = null
    var symbol: String? = null
    var unSymbol: String? = null
    var quoteMultiplier: String? = null
    var quoteLotSize: String? = null
    var weekly: String? = null
    var regionId: String? = null
    var exchangeCode: String? = null
    var subType: Int = 0
    var positionCostPrice: String? = null
    var stockSymbol: String? = null
    var currency: String? = null
    var lastPrice: String? = null
    var optionSymbol: String? = null
    var expirationType: String? = null
    var changeRatio: String? = null
    var isStdSettle: Int = 0
}

const val SUB_TYPE_INDEX_OPTION_CALL = 1
const val SUB_TYPE_INDEX_OPTION_PUT = 2
const val SUB_TYPE_FUTURES_OPTION_CALL = 3
const val SUB_TYPE_FUTURES_OPTION_PUT = 4
STUB
echo "  ✓ TickerOptionBean.kt"

# ---- TickerBase (Java) - 添加缺失字段 ----
DIR="$APP_SRC/com/webull/core/framework/bean"
mkdir -p "$DIR"
cat > "$DIR/TickerBase.java" << 'STUB'
package com.webull.core.framework.bean;

import java.io.Serializable;

/** Stub: 标的档案信息（从 AppDev3 同步字段） */
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
STUB
echo "  ✓ TickerBase.java"

# ---- TickerRealtimeV2 (Kotlin) ----
cat > "$DIR/TickerRealtimeV2.kt" << 'STUB'
package com.webull.core.framework.bean

/** Stub: 行情实时数据（从 AppDev3 同步字段） */
open class TickerRealtimeV2 : TickerBase() {
    var price: String? = null
    var pPrice: String? = null
    var close: String? = null
    var open: String? = null
    var high: String? = null
    var low: String? = null
    var nPrice: String? = null
    var status: String? = null
    var askList: ArrayList<TickerAskBid>? = null
    var bidList: ArrayList<TickerAskBid>? = null
    var nAskList: ArrayList<TickerAskBid>? = null
    var nBidList: ArrayList<TickerAskBid>? = null
}
STUB
echo "  ✓ TickerRealtimeV2.kt"

echo ""
echo "=== 完成！重新生成验证 ==="
echo "  rm -rf app/build/generated/ksp && ./gradlew :app:compileDebugJavaWithJavac"
