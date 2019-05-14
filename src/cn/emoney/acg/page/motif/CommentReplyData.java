package cn.emoney.acg.page.motif;

public class CommentReplyData extends GroupCommentData {
	private String mDstName = "";
	private int mDstId = 0;

	@Override
	public void reset() {
		super.reset();
		setDstId(0);
		setDstName("");
	}
	
	public String getDstName() {
		return mDstName;
	}

	public void setDstName(String mDstName) {
		this.mDstName = mDstName;
	}

	public int getDstId() {
		return mDstId;
	}

	public void setDstId(int mDstId) {
		this.mDstId = mDstId;
	}
}
