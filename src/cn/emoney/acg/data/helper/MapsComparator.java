package cn.emoney.acg.data.helper;

import java.util.Comparator;
import java.util.Map;

public class MapsComparator implements Comparator<Map<String, Object>>
{
	public final static int ASCENDING_ORDER = 1; //升序
	public final static int DESCENDING_ORDER = -1; //降序
	
	private UserCompare userCompare = null;
	private int sortType = ASCENDING_ORDER;
	
	public MapsComparator(int sortType, UserCompare userCompare)
	{
		this.userCompare = userCompare;
		this.sortType = sortType;
	}
	
	@Override
	public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
		// TODO Auto-generated method stub
		int type = 0;
		if (userCompare != null) {
			type = userCompare.getRealRelationship(lhs, rhs);
		}
		return type * sortType;
	}
	
	//请真实的返回两个对象的大小关系 如:
	//如a>b 请返回1
	//a==b 请返回0
	//a<b 请返回-1
	public static interface UserCompare
	{
		int getRealRelationship (Map<String, Object> map1, Map<String, Object> map2);
	}
}

