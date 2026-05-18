package com.webull.library.tradenetwork.bean.order;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.OptionLeg;
import com.webull.commonmodule.networkinterface.quoteapi.beans.option.TickerOptionBean;
import com.webull.commonmodule.trade.bean.OrderFeeDetails;
import com.webull.commonmodule.trade.bean.StConditionResponse;
import com.webull.commonmodule.trade.bean.UsConditionResponse;
import com.webull.core.framework.Constants;
import com.webull.core.framework.bean.TickerAskBid;
import com.webull.core.framework.bean.TickerBase;
import com.webull.core.framework.bean.TickerRealtimeV2;
import com.webull.library.trade.Constant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * https://pre-office.webullbroker.com/tradegw/docsPage?appName=ustrade-app-gw&group=order&id=254
 * 期权订单组合
 * Author: niefang
 * Date: 2019-10-10.
 */
public class OptionOrderGroupBean implements Serializable {

    /**
     * 组合ID
     */
    public String comboId;

    /**
     * 成交金额
     */
    public String filledAmount;

    /**
     * 组合ID
     */

    @SerializedName(value = "orderId", alternate = {"id"})
    public String orderId;

    /**
     * 组合类型 Long Calls，Long Puts等。
     */
    public String optionStrategy;

    /**
     * 期权类型 看涨为Call，看跌为Put
     */
    public String optionType;

    /**
     * 正股symbol
     */
    public TickerBase ticker;

    /**
     * 合约数量
     */

    @SerializedName(value = "quantity", alternate = {"totalQuantity"})
    public String quantity;

    /**
     * 已执行数量
     */
    public String filledQuantity;

    /**
     * 下单时间
     */

    @SerializedName(value = "orderTime", alternate = {"createTime", "placeTime"})
    public String orderTime;

    /**
     * 成交时间
     */
    public String filledTime;

    /**
     * 类型 股票为EQUITY，期权为OPTION
     */

    @SerializedName(value = "comboTickerType", alternate = {"tickerType", "securityType"})
    public String comboTickerType;

    public int subType;

    public int regionId;

    /**
     * 订单类型 MKT ：市价单；STP：止损单；LMT：限价单；STP LMT：止损限价单
     */
    public String orderType;

    /**
     * 止损价 定单类型为 STP 或 STP LMT 时必填
     */


    @SerializedName(value = "auxPrice", alternate = {"stopPrice"})
    public String auxPrice;

    /**
     * 限价 定单类型为 LMT 或 STP LMT 时必填
     */
    public String lmtPrice;

    /**
     * 订单有效期 DAY GTC
     */
    public String timeInForce;

    public String expireDate; // 期货新增：过期日，GTD订单类型必填 : yyyy/MM/dd
    public String expireTime; // 期货新增：过期日，GTD订单类型必填 : yyyy/MM/dd


    /**
     * 货币
     */
    public String currency;

    /**
     * 是否允许盘前盘后交易
     */
    public boolean outsideRegularTradingHour;

    /**
     * 是否可以取消
     */
    public boolean canCancel;

    /**
     * 是否可以修改
     */
    public boolean canModify;

    /**
     * BO代客下单是否允许客户端修改
     */
    public String allowClientModify;

    /**
     * 订单状态国际化
     */

    @SerializedName(value = "statusStr", alternate = {"statusName"})
    public String statusStr;

    /**
     * 订单状态码
     */

    @SerializedName(value = "status", alternate = {"statusCode"})
    public String status;

    /**
     * 下单失败原因
     */

    @SerializedName(value = "orderFailedReason", alternate = {"failedReason"})
    public String orderFailedReason;

    /**
     * 订单来源
     * BO 代客下单
     */
    public String orderSource;

    /**
     * 是否强平
     * NORMAL普通单,
     * LIQUIDATE强平单
     */
    public String orderCategory;

    /**
     * 撤单原因
     */
    public String cancelReason;


    /**
     * 订单数组
     */

    @SerializedName(value = "orders", alternate = {"legs"})
    public List<OptionOrderBean> orders;

    public String stockPrice;

    public String stockBid;

    public String stockAsk;

    /**
     * 买卖方向 买为BUY，卖为SELL
     */
    public String action;
    /**
     * 成交均价
     */

    @SerializedName(value = "avgFilledPrice", alternate = {"avgFillPrice"})
    public String avgFilledPrice;

    /**
     * leg in还是leg out  leg in为LI，leg out为LO
     */
    public String legInOrLegOut;
    /**
     * 期望的策略, 因为与 legin 业务互斥, 除去字段命名, 与 legin 业务相似, 故数据库rolling、legin
     * 复用共用此字段(rollingPositionId有值时, legInStrategy为rolling后的策略, legPositionId有值时, legInStrategy为legin的后策略)
     */
    public String legInStrategy;
    /**
     * 被leg in leg out的持仓ID
     */
    public String legPositionId;
    /**
     * 被leg in的来源，区分是股票还是期权，用来根据 legPositionId和legOrderTickerType，区分调用持仓接口
     * <br/>"legOrderTickerType":"leg_position_id 源标的类型 (OrderTickerType枚举: OPTION、EQUITY)"
     */

    @SerializedName(value = "legOrderTickerType")
    public String legInSource;

    /**
     * 用于展示期权订单详情页，被LegIn的腿信息
     * */
    public List<LegInItem> legInDetails;

    /**
     * 组合订单类型 主单：MASTER；止盈单：STOP_PROFIT；止损单：STOP_LOSS
     * @since 8.0.0 新增接口返回组合订单类型
     */
    public String comboType;

    /**
     * 实收手续费
     */
    public String fee;

    /**
     * 应收手续费
     */
    public String receivableFee;

    /**
     * 手续费明细
     * */
    public OrderFeeDetails feeDetail;

    /**
     * 期权的symbol
     */
    public String optionSymbol;

    /** Rolling 订单的目标持仓id， 不为空表示该订单为rolling订单*/
    public String rollingPositionId;

    /* 条件单参数 */
    public boolean isCondition;
    public boolean isConditionActive;
    public boolean canModifyCondition;

    //北美条件单参数
    public List<UsConditionResponse> conditions;

    //ST条件单参数
    @Nullable
    public List<StConditionResponse> conditionList;
    public boolean orderTrigger = false; // 基础条件单的条件是否已触发

    //跟踪止损限价单-指定价差
    public String trailingLimitPrice;
    @Nullable
    public String triggerPriceType;
    //期权大单是否拆单，false不拆，true拆单
    @Nullable
    public Boolean isNotHeld = null;
    //跟踪止损单- 相关字段
    public String trailingStopStep;
    @Nullable
    public String trailingType;

    /** 北美期货在用 */
    @Nullable
    public String latestAuxPrice; // 期权跟踪止损单-止损价

    /**
     * 构造简单信息的期权的腿列表
     *
     * @return
     */
    public List<OptionLeg> buildOptionLegSimpleList() {
        List<OptionLeg> resultList = new ArrayList<>();

        OptionLeg optionLeg;
        if (orders != null && !orders.isEmpty()) {

            TickerOptionBean tickerOptionBean;
            TickerRealtimeV2 tickerRealtimeV2;

            //股票腿时，每分合约对应的股票数
            String optionContractDeliverable = null;
            String optionContractMultiplier = "";

            for (OptionOrderBean order : orders) {
                if (order == null) {
                    continue;
                }

            }

            for (OptionOrderBean order : orders) {
                if (order == null) {
                    continue;
                }

                optionLeg = new OptionLeg();
                //股票为EQUITY，期权为OPTION
                resultList.add(optionLeg);
            }
        }

        return resultList;
    }

    public void fixData(TickerRealtimeV2 tickerRealtimeV2) {
        if (tickerRealtimeV2 == null) {
            return;
        }
        ArrayList<TickerAskBid> bidList = new ArrayList<>();
        TickerAskBid bid = new TickerAskBid();
        bid.setPrice(stockBid);
        //这里后端没有返回bid ask的size
        bidList.add(bid);

        ArrayList<TickerAskBid> askList = new ArrayList<>();
        TickerAskBid ask = new TickerAskBid();
        ask.setPrice(stockAsk);
        //这里后端没有返回bid ask的size
        askList.add(ask);

        tickerRealtimeV2.setBidList(bidList);
        tickerRealtimeV2.setAskList(askList);
        tickerRealtimeV2.setClose(stockPrice);
    }

    // 获取正股id
    public String getStockTickerId() {
        return  "";
    }

    @Override
    public String toString() {
        return "OptionOrderGroupBean{" +
                "comboId='" + comboId + '\'' +
                ", optionStrategy='" + optionStrategy + '\'' +
                ", ticker=" + ticker +
                ", quantity='" + quantity + '\'' +
                ", filledQuantity='" + filledQuantity + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", comboTickerType='" + comboTickerType + '\'' +
                ", orderType='" + orderType + '\'' +
                ", auxPrice='" + auxPrice + '\'' +
                ", lmtPrice='" + lmtPrice + '\'' +
                ", timeInForce='" + timeInForce + '\'' +
                ", currency='" + currency + '\'' +
                ", outsideRegularTradingHour='" + outsideRegularTradingHour + '\'' +
                ", canCancel=" + canCancel +
                ", canModify=" + canModify +
                ", orderFailedReason='" + orderFailedReason + '\'' +
                ", orderSource='" + orderSource + '\'' +
                ", orderCategory='" + orderCategory + '\'' +
                ", cancelReason='" + cancelReason + '\'' +
                ", orders=" + orders +
                ", stockPrice='" + stockPrice + '\'' +
                ", stockBid='" + stockBid + '\'' +
                ", stockAsk='" + stockAsk + '\'' +
                '}';
    }

    public String getUserOrderPrice() {
        String resultPrice = null;


        if (!TextUtils.isEmpty(orderType)) {
            switch (orderType) {
                case Constant.OrderConstant.LMT_TYPE:
                case Constant.OrderConstant.STPLMT_TYPE:
                    resultPrice = lmtPrice;
                    break;
                case Constant.OrderConstant.STP_TYPE:
                    resultPrice = auxPrice;
                    break;
            }
        }
        return resultPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    /**
     * 是否显示AM
     */
    public boolean isShowAmFlag(){
        if (orders == null || orders.isEmpty()) {
            return false;
        }

        for (OptionOrderBean order : orders) {
            if (order == null) {
                return false;
            }

        }
        return true;
    }

    /**
     * 判断是否是由期权创建的Leg In 单
     *
     * @return true：期权创建的 Leg In 单
     */
    public boolean isLegInFromOption() {
        return "option".equalsIgnoreCase(legInSource)
                || isLegInFromMultiOption();
    }

    public boolean isLegInFromMultiOption() {
        return false;
    }

    public boolean isRollingOrder() {
        return !TextUtils.isEmpty(rollingPositionId);
    }

//    public int getAction() {
//        if (Constants.OptionConstant.SideConstant.TEXT_BUY.equalsIgnoreCase(action)) {
//            return Constants.OptionConstant.SideConstant.BUY;
//        }
//        return Constants.OptionConstant.SideConstant.SELL;
//    }

    /**
     * 获取Rolling 订单的 Call/Put
     */
    @NonNull
    public String getRollingCallOrPut() {
        if (Constants.OptionConstant.OptionStrategy.Strategy_Simple.equalsIgnoreCase(legInStrategy)
                || Constants.OptionConstant.OptionStrategy.Strategy_Covered.equalsIgnoreCase(legInStrategy)) {
            // Rolling 订单的 看涨/看跌 是一致的，所以取其中任意一条均可以
            return orders.get(0).optionType;
        }
        return "";
    }

    /**
     * 获取 Rolling 腿的买卖方向作为 Rolling的方向
     */
    public int getRollingAction() {
        // 取 Rolling 订单腿后半部分第一条腿买卖方向显示。需服务器保证前半部分是平仓腿，经和服务端沟通先取后半部分作为建仓腿
        int index = orders.size() / 2;
        if (Constants.OptionConstant.SideConstant.TEXT_BUY.equalsIgnoreCase(orders.get(index).action))
            return Constants.OptionConstant.SideConstant.BUY;
        else return Constants.OptionConstant.SideConstant.SELL;
    }

    /** 获取止损价 */
    public @Nullable String getAuxPrice() {
        if ("STP".equalsIgnoreCase(orderType) || "STP LMT".equalsIgnoreCase(orderType)) {
            if (conditionList != null) { // 止损单
                for (StConditionResponse conditionData : conditionList) {
                    if ("BASE".equalsIgnoreCase(conditionData.getConditionType()) && "VALUE".equalsIgnoreCase(conditionData.getCompareFieldType())) { // 并且找到了基础条件
                        if (!TextUtils.isEmpty(conditionData.getCompareValue())) return conditionData.getCompareValue();
                    }
                }
            }
        }
        // 找不到用原始值兜底
        return auxPrice;
    }

    public void setAuxPrice(@Nullable String auxPrice) {
        if ("STP".equalsIgnoreCase(orderType) || "STP LMT".equalsIgnoreCase(orderType)) {
            if (conditionList != null) { // 止损单
                for (StConditionResponse conditionData : conditionList) {
                    if ("BASE".equalsIgnoreCase(conditionData.getConditionType()) && "VALUE".equalsIgnoreCase(conditionData.getCompareFieldType())) { // 并且找到了基础条件
                        conditionData.setCompareValue(auxPrice);
                    }
                }
            }
        }
        this.auxPrice = auxPrice;
    }

}
