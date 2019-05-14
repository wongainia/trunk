package cn.emoney.acg.data;

import cn.emoney.acg.data.protocol.quiz.QuizDefine.Item;
import cn.emoney.acg.data.quiz.QuizContentInfo;

/**
 * 存储QuotePage的ListView中每一个item包含的数据
 * */
public class QuoteItemBean {
	
	/**
	 * item类型： 1. 个股新闻/公告/研报   2. 个股问答
	 * */
	public int itemType;
	
	/*
	 * 下面的属性使用在个股新闻/公告/研报中
	 * */
	
	/**
	 * 个股消息对应的资源地址
	 * */
	public String url;
	
	/**
	 * 个股消息标题
	 * */
	public String title;
	
	/**
	 * 个股消息时间
	 * */
	public String time;
	
	/**
	 * 个股消息发布方
	 * */
	public String from;
	
	/**
	 * 个股消息类型
	 * */
	public String sortcls;
	
	/**
	 * 最后一次网络请求返回的end
	 * */
	public int end;
	
	/**
	 * 最后一次网络请求返回的数量个数
	 * */
	public int lastReceiveItemCount;
	
	/**
	 * 消息对应的序列号
	 * */
	public int sortId;
	
	/*
	 * 下面属性在问答中使用
	 * */
	public QuizContentInfo quizItem;

	public QuoteItemBean(int itemType, String url, String title, String time,
			String from, String sortcls, int end, int lastReceiveItemCount, int sortId) {
		this.itemType = itemType;
		this.url = url;
		this.title = title;
		this.time = time;
		this.from = from;
		this.sortcls = sortcls;
		this.end = end;
		this.lastReceiveItemCount = lastReceiveItemCount;
		this.sortId = sortId;
	}
	
	public QuoteItemBean(int itemType, Item item) {
        this.itemType = itemType;
        this.quizItem = QuizContentInfo.initOfServerItem(item);
    }

}
