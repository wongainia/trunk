package cn.emoney.acg.data;

import java.util.ArrayList;

import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;


public class RequestBuilder {

	public static DynaValueData_Request dynaValueDataRequestBuilder(int classType, int groupType, ArrayList<Integer> goodsIds,
			ArrayList<Integer> reqFileds, int sortField, Boolean sortOrder,
			int reqBegin, int reqSize, int lastUpdateMarketTime,
			int lastUpdateMarketDate){
		DynaValueData_Request gr = DynaValueData_Request.newBuilder()
				.setClassType(classType).setGroupType(groupType)
				.addAllGoodsId(goodsIds).addAllReqFields(reqFileds)
				.setSortField(sortField).setSortOrder(sortOrder)
				.setReqBegin(reqBegin).setReqSize(reqSize)
				.setLastUpdateMarketTime(lastUpdateMarketTime)
				.setLastUpdateMarketDate(lastUpdateMarketDate).build();
		return gr;
	}
	
}
