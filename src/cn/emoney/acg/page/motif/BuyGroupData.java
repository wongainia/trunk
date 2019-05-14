package cn.emoney.acg.page.motif;

import java.io.Serializable;

public class BuyGroupData implements Serializable {
    private String mTotalZDF = "0";
    private String mDayZDF = "0";
    private String mWeekZDF = "0";
    private String mMonthZDF = "0";
    private String mGroupName = "";
    private String mCreateTime = "";
    private String mCreator = "";
    private int mCreatorId = 0;
    private String m_strFocus = "";
    private long m_iFocusNum = 0;
    private long m_iPraise = 0;
    /**
     * 1:推荐 2:new 3:hot 0:无
     */
    private int mGroupState = 0;
    private int mGroupId = 0;
    private String mGroupIdea = "";

    public String getTotalZDF() {
        return mTotalZDF;
    }

    public void setTotalZDF(String mTotalZDF) {
        this.mTotalZDF = mTotalZDF;
    }

    public String getDayZDF() {
        return mDayZDF;
    }

    public void setDayZDF(String mDayZDF) {
        this.mDayZDF = mDayZDF;
    }

    public String getWeekZDF() {
        return mWeekZDF;
    }

    public void setWeekZDF(String mWeekZDF) {
        this.mWeekZDF = mWeekZDF;
    }

    public String getMonthZDF() {
        return mMonthZDF;
    }

    public void setMonthZDF(String mMonthZDF) {
        this.mMonthZDF = mMonthZDF;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String mGroupName) {
        this.mGroupName = mGroupName;
    }

    public String getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(String mCreateTime) {
        this.mCreateTime = mCreateTime;
    }

    public String getStrFocus() {
        return m_strFocus;
    }

    public void setStrFocus(String m_strFocus) {
        this.m_strFocus = m_strFocus;
    }

    public long getiFocusNum() {
        return m_iFocusNum;
    }

    public void setiFocusNum(long m_iFocusNum) {
        this.m_iFocusNum = m_iFocusNum;
    }

    public long getPraise() {
        return m_iPraise;
    }

    public void setPraise(long m_iPraise) {
        this.m_iPraise = m_iPraise;
    }

    /**
     * 
     * @return 1:推荐 2:new 3:hot 0:无
     */
    public int getGroupState() {
        return mGroupState;
    }

    /**
     * 
     * @param mGroupState 1:推荐 2:new 3:hot 0:无
     */
    public void setGroupState(int mGroupState) {
        this.mGroupState = mGroupState;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int mGroupId) {
        this.mGroupId = mGroupId;
    }

    public String getGroupIdea() {
        return mGroupIdea;
    }

    public void setGroupIdea(String mGroupIdea) {
        this.mGroupIdea = mGroupIdea;
    }

    public String getCreator() {
        return mCreator;
    }

    public void setCreator(String mCreator) {
        this.mCreator = mCreator;
    }

    public int getCreatorId() {
        return mCreatorId;
    }

    public void setCreatorId(int mCreatorId) {
        this.mCreatorId = mCreatorId;
    }
}
