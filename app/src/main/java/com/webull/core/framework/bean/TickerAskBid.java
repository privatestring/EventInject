package com.webull.core.framework.bean;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/7/3
 */
public class TickerAskBid implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    public String price;
    public String volume;
    public String quoteEx;
    public String bkCount = "--";
    private String order;

    public String oriPrice; // 企业债债券加点前的价格
    public String bondYield;//债券收益率
    public String size;//债券行情报价的数量
    private List<TickerOrderItem> orderItems;

    public List<TickerOrderItem> getOrderItems() {
        if (orderItems == null && !TextUtils.isEmpty(order)) {
            orderItems = parseToOrderItem(order);
        }
        return orderItems;
    }

    public void setOrderItems(List<TickerOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getOrder() {
        return order;
    }


    public TickerAskBid() {
    }

    public TickerAskBid(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getQuoteEx() {
        return quoteEx;
    }

    public void setQuoteEx(String quoteEx) {
        this.quoteEx = quoteEx;
    }

    public String getBkCount() {
        return bkCount;
    }

    @NonNull
    @Override
    public TickerAskBid clone() {
        try {
            return (TickerAskBid) super.clone();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }


    public static List<TickerOrderItem> parseToOrderItem(String order) {
        if (!TextUtils.isEmpty(order)) {
            List<TickerOrderItem> list = new ArrayList<>();
            String[] items = order.split("\\|");
            for (String item : items) {
                if (item.contains(",")) {
                    String[] orderItemString = item.split(",");
                    TickerOrderItem orderItem = new TickerOrderItem();
                    try {
                        orderItem.setMarketName(Integer.valueOf(orderItemString[0]));
                        orderItem.setVolume(Integer.valueOf(orderItemString[1]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    list.add(orderItem);
                }
            }

            return list;
        }
        return null;
    }
}