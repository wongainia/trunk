package cn.emoney.acg.page.motif;

import java.io.Serializable;

public class GroupCommentData implements Cloneable, Serializable {
    private int mId = 0;
    private int mBelongGroupCode = 0;
    private String mContent = "";
    private int mPublisherId = 0;
    private String mPublisherName = "";
    private String mPublisherHeaderId = "0";
    private int mReplyCount = 0;
    private String mPublishTime = "";

    public void reset() {
        setBelongGroupCode(0);
        setContent("");
        setId(0);
        setPublisherHeaderId("0");
        setPublisherId(0);
        setPublisherName("");
        setPublishTime("");
        setReplyCount(0);
    }

    public int getId() {
        return mId;
    }

    public int getBelongGroupCode() {
        return mBelongGroupCode;
    }

    public String getContent() {
        return mContent;
    }

    public int getPublisherId() {
        return mPublisherId;
    }

    public String getPublisherName() {
        return mPublisherName;
    }

    public String getPublisherHeaderId() {
        return mPublisherHeaderId;
    }

    public int getReplyCount() {
        return mReplyCount;
    }

    public String getPublishTime() {
        return mPublishTime;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public void setBelongGroupCode(int mBelongGroupCode) {
        this.mBelongGroupCode = mBelongGroupCode;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public void setPublisherId(int mPublisherId) {
        this.mPublisherId = mPublisherId;
    }

    public void setPublisherName(String mPublisherName) {
        this.mPublisherName = mPublisherName;
    }

    public void setPublisherHeaderId(String mPublisherHeaderId) {
        this.mPublisherHeaderId = mPublisherHeaderId;
    }

    public void setReplyCount(int mReplyCount) {
        this.mReplyCount = mReplyCount;
    }

    public void setPublishTime(String mPublishTime) {
        this.mPublishTime = mPublishTime;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }

    }
}
