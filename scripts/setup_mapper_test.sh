#!/bin/bash
# ============================================================
# Mapper 测试用例生成脚本
# 
# 功能：
# 1. 将 source/mapper/code/ 下的 5 个 Mapper 文件复制到 com/joker/event/mapper/ 并改包名
# 2. 从源项目 AppDev3 中提取 Mapper 引用的 bean 类，生成精简 stub 放到对应包路径
#
# 使用方式：
#   cd /Users/joker/webull/webull/inject/EventInject
#   bash scripts/setup_mapper_test.sh
# ============================================================

set -e

PROJECT_DIR="/Users/joker/webull/webull/inject/EventInject"
APP_SRC="$PROJECT_DIR/app/src/main/java"
SOURCE_DIR="$PROJECT_DIR/source/mapper/code"
APPDEV3="/Users/joker/webull/git/AppDev3"

echo "=== Step 1: 复制 Mapper 文件到 com/joker/event/mapper/ 并改包名 ==="

MAPPER_DIR="$APP_SRC/com/joker/event/mapper"
mkdir -p "$MAPPER_DIR"

for file in "$SOURCE_DIR"/*.kt; do
    filename=$(basename "$file")
    # 读取原始包名
    original_pkg=$(grep "^package " "$file" | head -1 | sed 's/package //')
    # 替换包名为 com.joker.event.mapper
    sed "s/^package .*/package com.joker.event.mapper/" "$file" > "$MAPPER_DIR/$filename"
    echo "  ✓ $filename (原包: $original_pkg)"
done

echo ""
echo "=== Step 2: 生成 stub bean 类 ==="

# ---- KBusQuoteBean ----
DIR="$APP_SRC/com/webull/trade/bean/common/realtime"
mkdir -p "$DIR"
cat > "$DIR/KBusQuoteBean.kt" << 'STUB'
package com.webull.trade.bean.common.realtime

/** Stub: KBus 行情数据 */
class KBusQuoteBean {
    var tickerId: String? = null
    var tradePrice: String? = null
    var tradeClose: String? = null
    var open: String? = null
    var high: String? = null
    var low: String? = null
    var ask: String? = null
    var askList: String? = null
    var bid: String? = null
    var bidList: String? = null
    var status: String? = null
    var nightPrice: String? = null
    var nightBidList: String? = null
    var nightBid: String? = null
    var nightAskList: String? = null
    var nightAsk: String? = null
}
STUB
echo "  ✓ KBusQuoteBean.kt"

# ---- TickerAskBid ----
DIR="$APP_SRC/com/webull/core/framework/bean"
mkdir -p "$DIR"
cat > "$DIR/TickerAskBid.java" << 'STUB'
package com.webull.core.framework.bean;

import java.io.Serializable;

/** Stub: 买卖盘数据 */
public class TickerAskBid implements Serializable {
    public String price;
    public String volume;
}
STUB
echo "  ✓ TickerAskBid.java"

# ---- TickerBase ----
cat > "$DIR/TickerBase.java" << 'STUB'
package com.webull.core.framework.bean;

import java.io.Serializable;

/** Stub: 标的档案信息（精简版） */
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
}
STUB
echo "  ✓ TickerBase.java"

# ---- TickerRealtimeV2 ----
cat > "$DIR/TickerRealtimeV2.kt" << 'STUB'
package com.webull.core.framework.bean

/** Stub: 行情实时数据（精简版） */
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

# ---- CommonPositionBean ----
DIR="$APP_SRC/com/webull/commonmodule/trade/bean"
mkdir -p "$DIR"
cat > "$DIR/CommonPositionBean.java" << 'STUB'
package com.webull.commonmodule.trade.bean;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Stub: 通用持仓 Bean */
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

# ---- CommonPositionGroupBean ----
cat > "$DIR/CommonPositionGroupBean.java" << 'STUB'
package com.webull.commonmodule.trade.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Stub: 通用持仓组 Bean */
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

# ---- CommonOrderBean ----
cat > "$DIR/CommonOrderBean.java" << 'STUB'
package com.webull.commonmodule.trade.bean;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;
import java.util.List;

/** Stub: 通用订单 Bean */
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
    public Integer subType;
    public TickerBase ticker;

    public String getUnSymbol() { return underlyingSymbol; }
}
STUB
echo "  ✓ CommonOrderBean.java"

# ---- CommonOrderGroupBean ----
cat > "$DIR/CommonOrderGroupBean.java" << 'STUB'
package com.webull.commonmodule.trade.bean;

import java.io.Serializable;
import java.util.List;

/** Stub: 通用订单组 Bean */
public class CommonOrderGroupBean implements Serializable {
    public String comboId;
    public String comboTickerType;
    public String optionStrategy;
    public List<CommonOrderBean> orders;
}
STUB
echo "  ✓ CommonOrderGroupBean.java"

# ---- OptionPositionBean ----
DIR="$APP_SRC/com/webull/library/tradenetwork/bean/option"
mkdir -p "$DIR"
cat > "$DIR/OptionPositionBean.java" << 'STUB'
package com.webull.library.tradenetwork.bean.option;

import com.webull.core.framework.bean.TickerRealtimeV2;
import java.io.Serializable;

/** Stub: 期权持仓 Bean */
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

# ---- OptionPositionGroupBean ----
cat > "$DIR/OptionPositionGroupBean.java" << 'STUB'
package com.webull.library.tradenetwork.bean.option;

import java.io.Serializable;
import java.util.List;

/** Stub: 期权持仓组 Bean */
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

# ---- OptionOrderBean ----
DIR="$APP_SRC/com/webull/library/tradenetwork/bean/order"
mkdir -p "$DIR"
cat > "$DIR/OptionOrderBean.java" << 'STUB'
package com.webull.library.tradenetwork.bean.order;

import com.webull.core.framework.bean.TickerBase;
import java.io.Serializable;

/** Stub: 期权订单 Bean */
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

    public String getUnSymbol() { return underlyingSymbol; }
}
STUB
echo "  ✓ OptionOrderBean.java"

# ---- OptionOrderGroupBean ----
cat > "$DIR/OptionOrderGroupBean.java" << 'STUB'
package com.webull.library.tradenetwork.bean.order;

import java.io.Serializable;
import java.util.List;

/** Stub: 期权订单组 Bean */
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
    public OptionOrderBean ticker;
    public List<OptionOrderBean> orders;
}
STUB
echo "  ✓ OptionOrderGroupBean.java"

# ---- TickerOptionBean ----
DIR="$APP_SRC/com/webull/commonmodule/networkinterface/quoteapi/beans/option"
mkdir -p "$DIR"
cat > "$DIR/TickerOptionBean.kt" << 'STUB'
package com.webull.commonmodule.networkinterface.quoteapi.beans.option

/** Stub: 期权行情 Bean */
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
}

const val SUB_TYPE_INDEX_OPTION_CALL = 1
const val SUB_TYPE_INDEX_OPTION_PUT = 2
const val SUB_TYPE_FUTURES_OPTION_CALL = 3
const val SUB_TYPE_FUTURES_OPTION_PUT = 4
STUB
echo "  ✓ TickerOptionBean.kt"

# ---- PlaceOrder ----
DIR="$APP_SRC/com/webull/library/tradenetwork/bean"
mkdir -p "$DIR"
cat > "$DIR/PlaceOrder.java" << 'STUB'
package com.webull.library.tradenetwork.bean;

import java.io.Serializable;

/** Stub: 下单请求 Bean */
public class PlaceOrder implements Serializable {
    public String action;
    public String quantityType;
    public String quantity;
    public String timeInForce;
    public String orderType;
    public String lmtPrice;
    public String auxPrice;
}
STUB
echo "  ✓ PlaceOrder.java"

# ---- PlaceOrderFormParams ----
DIR="$APP_SRC/com/webull/order/place/dependency/entry"
mkdir -p "$DIR"
cat > "$DIR/PlaceOrderFormParams.kt" << 'STUB'
package com.webull.order.place.dependency.entry

import com.webull.library.repository.constant.AtQuantityType
import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import java.math.BigDecimal

/** Stub: 下单表单参数 */
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
}
STUB
echo "  ✓ PlaceOrderFormParams.kt"

# ---- SnapshotFormParams ----
cat > "$DIR/SnapshotFormParams.kt" << 'STUB'
package com.webull.order.place.dependency.entry

import com.webull.library.repository.constant.OrderActionEnum
import com.webull.library.repository.constant.TimeInForceEnum
import java.math.BigDecimal

/** Stub: 快照表单参数 */
class SnapshotFormParams {
    var orderAction: OrderActionEnum? = null
    var quantity: BigDecimal? = null
    var timeInForce: TimeInForceEnum? = null
    var lmtPriceAtLimitOrder: BigDecimal? = null
    var auxPriceAtStopOrder: BigDecimal? = null
    var trailingStopStep: String? = null
    var trailingType: String? = null
}
STUB
echo "  ✓ SnapshotFormParams.kt"

# ---- OrderActionEnum / AtQuantityType / TimeInForceEnum ----
DIR="$APP_SRC/com/webull/library/repository/constant"
mkdir -p "$DIR"
cat > "$DIR/OrderActionEnum.kt" << 'STUB'
package com.webull.library.repository.constant

/** Stub: 订单操作枚举 */
enum class OrderActionEnum(val constant: String) {
    BUY("BUY"), SELL("SELL");
    companion object {
        @JvmStatic fun find(value: String?): OrderActionEnum? = entries.firstOrNull { it.constant == value }
    }
}
STUB

cat > "$DIR/AtQuantityType.kt" << 'STUB'
package com.webull.library.repository.constant

/** Stub: 数量类型枚举 */
enum class AtQuantityType(val constant: String) {
    SHARES("SHARES"), AMOUNT("AMOUNT");
    companion object {
        @JvmStatic fun find(value: String?): AtQuantityType? = entries.firstOrNull { it.constant == value }
    }
}
STUB

cat > "$DIR/TimeInForceEnum.kt" << 'STUB'
package com.webull.library.repository.constant

/** Stub: 有效期枚举 */
enum class TimeInForceEnum(val constant: String) {
    DAY("DAY"), GTC("GTC");
    companion object {
        @JvmStatic fun find(value: String?): TimeInForceEnum? = entries.firstOrNull { it.constant == value }
    }
}
STUB
echo "  ✓ OrderActionEnum / AtQuantityType / TimeInForceEnum"

# ---- Constant (trade) ----
DIR="$APP_SRC/com/webull/library/trade"
mkdir -p "$DIR"
cat > "$DIR/Constant.java" << 'STUB'
package com.webull.library.trade;

/** Stub: 交易常量 */
public class Constant {
    public static class OrderConstant {
        public static final String LMT_TYPE = "LMT";
        public static final String STP_TYPE = "STP";
        public static final String STPLMT_TYPE = "STP LMT";
    }
}
STUB
echo "  ✓ Constant.java (trade)"

# ---- Constants (core framework) ----
DIR="$APP_SRC/com/webull/core/framework"
mkdir -p "$DIR"
cat > "$DIR/Constants.java" << 'STUB'
package com.webull.core.framework;

/** Stub: 核心常量 */
public class Constants {
    public static class OptionConstant {
        public static final String CALL = "call";
        public static final String PUT = "put";
        public static class OptionCycle {
            public static final int WEEKLY = 1;
            public static final int END_OF_MONTH = 4;
        }
    }
    public static class RegionConstants {
        public static final int REGION_HK = 2;
    }
}
STUB
echo "  ✓ Constants.java (core)"

# ---- TickerUtils ----
DIR="$APP_SRC/com/webull/core/utils"
mkdir -p "$DIR"
cat > "$DIR/TickerUtils.java" << 'STUB'
package com.webull.core.utils;

/** Stub: Ticker 工具类 */
public class TickerUtils {
    public static final int TICKER_TYPE_INDEX = 3;
    public static final int TICKER_TYPE_FUTURES = 5;
    public static final String OPTION_EXCHANGE_CODE = "OPRA";
}
STUB
echo "  ✓ TickerUtils.java"

# ---- GsonUtils ----
DIR="$APP_SRC/com/webull/networkapi/utils"
mkdir -p "$DIR"
cat > "$DIR/GsonUtils.kt" << 'STUB'
package com.webull.networkapi.utils

import java.lang.reflect.Type

/** Stub: Gson 工具类 */
object GsonUtils {
    @JvmStatic
    fun <T> fromLocalJson(json: String?, type: Type): T? = null
}
STUB
echo "  ✓ GsonUtils.kt"

# ---- format utils ----
DIR="$APP_SRC/com/webull/format/utils"
mkdir -p "$DIR"
cat > "$DIR/FormatUtils.kt" << 'STUB'
package com.webull.format.utils

import java.math.BigDecimal

/** Stub: 格式化工具扩展 */
fun String?.parseBigDecimal(replace: String = "0"): BigDecimal? {
    return try { this?.toBigDecimal() } catch (_: Exception) { replace.toBigDecimal() }
}

fun String?.parseBigDecimalNullable(): BigDecimal? {
    return try { this?.toBigDecimal() } catch (_: Exception) { null }
}

fun BigDecimal?.parseInt(): Int? = this?.toInt()

fun Int?.orZero(): Int = this ?: 0
STUB
echo "  ✓ FormatUtils.kt"

# ---- trade logger ----
DIR="$APP_SRC/com/webull/trade/common/logger"
mkdir -p "$DIR"
cat > "$DIR/TradeLogExt.kt" << 'STUB'
package com.webull.trade.common.logger

/** Stub: 交易日志扩展 */
fun String.tradeLogE(tag: String) { /* stub */ }
STUB
echo "  ✓ TradeLogExt.kt"

# ---- PlaceOrderLogNetTag ----
DIR="$APP_SRC/com/webull/order/place/framework/log"
mkdir -p "$DIR"
cat > "$DIR/PlaceOrderLogNetTag.kt" << 'STUB'
package com.webull.order.place.framework.log

/** Stub: 日志 tag */
const val PlaceOrderLogNetTag = "PlaceOrderLogNet"
STUB
echo "  ✓ PlaceOrderLogNetTag.kt"

echo ""
echo "=== Step 3: 验证文件结构 ==="
echo "Mapper 文件:"
ls "$MAPPER_DIR"/*.kt
echo ""
echo "Stub bean 文件:"
find "$APP_SRC/com/webull" -name "*.kt" -o -name "*.java" | sort
echo ""
echo "=== 完成！运行以下命令验证 KSP 生成 ==="
echo "  cd $PROJECT_DIR"
echo "  rm -rf app/build/generated/ksp"
echo "  ./gradlew :app:kspDebugKotlin"
echo "  find app/build/generated/ksp -name '*MapperImpl*'"
