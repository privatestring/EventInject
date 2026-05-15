package com.webull.core.framework.bean;

import java.io.Serializable;

/**
 * jamin
 * 2021-6-29
 */
public class BboBidAskItem implements Serializable {
    private String sip;
    private String p;
    private int v;
    private int l;

    public String getSip() {
        return sip;
    }

    public void setSip(String sip) {
        this.sip = sip;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public String getPrice() {
        return p;
    }

    public String getVolume() {
        return String.valueOf(v);
    }

    public String getQuoteEx() {
        return sip;
    }
}
