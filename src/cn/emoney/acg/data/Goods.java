package cn.emoney.acg.data;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.QuoteUtils;

public class Goods implements Parcelable {
    private int goodsId;
    private String goodsName;
    private String goodsCode;
    private String mLastClose = "0";
    private String mZxj = "0";
    private String mZdf = "0";
    private String mZd = "0";
    private String mJl = "0";
    private String mHsl = "0";
    private String mSyl = "0";
    private String mSjl = "0";
    private String mFiveZdf = "0";

    private String mPositionAmount = "0";
    private String mPositionPrice = "0.00";

    private String mPositionProfitLoss = "0";
    private String mPositionProfitLossPercent = "0.00%";
    private String mPositionMarketValue = "0";

    private int mBKId = 0;
    private String mBKName = "";
    private int mDayBS = 0;

    private int mQuoteColor = Color.GRAY;
    private List<String> mLstTypes = new ArrayList<String>();

    /**
     * 该支股票手机版本是否支持，默认为true
     * */
    private boolean isSupport = true;

    public Goods(int id, String name) {
        goodsId = id;
        goodsName = name;

        if (DataUtils.IsBK(goodsId)) {
            goodsCode = DataUtils.format_BK_GoodCode(goodsId);
        } else {
            goodsCode = QuoteUtils.getStockCodeByGoodsId(String.valueOf(goodsId));
        }

    }

    public void setGoodsName(String name) {
        goodsName = name;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public String getZxj() {
        return mZxj;
    }

    public void setZxj(String zxj) {
        mZxj = zxj;
    }

    public String getLastClose() {
        return mLastClose;
    }

    public void setLastClose(String lastClose) {
        mLastClose = lastClose;
    }

    public String getZdf() {
        return mZdf;
    }

    public void setZdf(String zdf) {
        mZdf = zdf;
    }

    public String getZd() {
        return mZd;
    }

    public void setZd(String zd) {
        mZd = zd;
    }

    public String getSyl() {
        return mSyl;
    }

    public void setSyl(String syl) {
        mSyl = syl;
    }

    public String getSjl() {
        return mSjl;
    }

    public void setSjl(String sjl) {
        mSjl = sjl;
    }

    public String getJl() {
        return mJl;
    }

    public void setJl(String jl) {
        mJl = jl;
    }

    public String getHsl() {
        return mHsl;
    }

    public void setHsl(String hsl) {
        mHsl = hsl;
    }

    public String getFiveZdf() {
        return mFiveZdf;
    }

    public void setFiveZdf(String zdf5) {
        mFiveZdf = zdf5;
    }

    public Goods(int goodsId, String goodsName, String goodsCode, String zxj, String zdf, String jl, String hsl, String fiveZdf) {
        this(goodsId, goodsName);
        this.goodsCode = goodsCode;
        this.mZxj = zxj;
        this.mZdf = zdf;
        this.mJl = jl;
        this.mHsl = hsl;
        this.mFiveZdf = fiveZdf;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

    }

    public void addType(String type) {
        if (!hasType(type)) {
            mLstTypes.add(type);
        }
    }

    public void addTypes(List<String> types) {
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            addType(type);
        }
    }

    public List<String> getTypes() {
        return mLstTypes;
    }

    public void delType(String type) {
        if (hasType(type)) {
            mLstTypes.remove(type);
        }
    }

    public boolean hasType(String type) {
        return mLstTypes.contains(type);
    }

    public void clearTypes() {
        mLstTypes.clear();
    }

    public void setPositionAmount(String amount) {
        mPositionAmount = amount;
    }

    public String getPositionAmount() {
        return mPositionAmount;
    }

    public void setPositionPrice(String price) {
        mPositionPrice = price;
    }

    public String getPositionPrice() {
        return mPositionPrice;
    }

    public void setPositionProfitLoss(String s) {
        mPositionProfitLoss = s;
    }

    public void setPositionProfitLossPercent(String s) {
        mPositionProfitLossPercent = s;
    }

    public void setPositionMarketValue(String s) {
        mPositionMarketValue = s;
    }

    public String getPositionProfitLoss() {
        return mPositionProfitLoss;
    }

    public String getPositionProfitLossPercent() {
        return mPositionProfitLossPercent;
    }

    public String getPositionMarketValue() {
        return mPositionMarketValue;
    }

    public int getBKId() {
        return mBKId;
    }

    public void setBKId(int id) {
        mBKId = id;
    }

    public String getBKName() {
        return mBKName;
    }

    public void setBKName(String name) {
        mBKName = name;
    }

    public void setDayBS(int dayBS) {
        mDayBS = dayBS;
    }

    public int getDayBS() {
        return mDayBS;
    }

    public int getQuoteColor() {
        return mQuoteColor;
    }

    public void setQuoteColor(int color) {
        mQuoteColor = color;
    }

    public void setSupport(boolean isSupport) {
        this.isSupport = isSupport;
    }

    public boolean isSupport() {
        return isSupport;
    }
}
