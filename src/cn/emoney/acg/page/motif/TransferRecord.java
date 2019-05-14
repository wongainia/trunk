package cn.emoney.acg.page.motif;

public class TransferRecord {
	public static String[] TRADE_TYPE = {"- -", "建仓", "加仓", "平仓", "减仓" };

	private int id; // 调仓记录号
	private String date; // 日期 YYYY-MM-DD HH:mm 或 MM-DD HH:mm
	private int goodid; // 股票代码，带市场前缀
	private String goodname; // 股票代码，带市场前缀
	private String bidPrice;// 成交价格， 单位分
	private int posSrc; // 原始仓位比率， 单位：万分之一，
	private int posDst; // 目标仓位比例, 单位：万分之一，
	private int tradeType; // 1:建仓 ; 2:加仓; 3:平仓; 4:减仓
	private String sTradeType = "- -"; // 1:建仓 ; 2:加仓; 3:平仓; 4:减仓
	private String reason; // 理由
	private int transferStatus;    // 调仓交易状态

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getGoodid() {
		return goodid;
	}

	public void setGoodid(int goodid) {
		this.goodid = goodid;
	}

	public String getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(String bidPrice) {
		this.bidPrice = bidPrice;
	}

	public int getPosSrc() {
		return posSrc;
	}

	public void setPosSrc(int posSrc) {
		this.posSrc = posSrc;
	}

	public int getPosDst() {
		return posDst;
	}

	public void setPosDst(int posDst) {
		this.posDst = posDst;
	}

	public int getTradeType() {
		return tradeType;
	}

	public void setTradeType(int iTradeType) {
		this.tradeType = iTradeType;

		if (0 <= iTradeType && iTradeType <= 4) {
			sTradeType = TRADE_TYPE[iTradeType];
		} else {
			sTradeType = "- -";
		}
	}

	public String getStrTradeType() {
		return sTradeType;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getGoodname() {
		return goodname;
	}

	public void setGoodname(String goodname) {
		this.goodname = goodname;
	}

    public int getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(int transferStatus) {
        this.transferStatus = transferStatus;
    }
}
