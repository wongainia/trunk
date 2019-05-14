package cn.emoney.acg.page.share.infodetail;


import android.os.Parcel;
import cn.emoney.sky.libs.network.data.JsonData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class InfoDetailJson extends JsonData {

	public int mCode = -1;
	public String mMsg = "未知错误";
	public String mTitle = "";
	public String mContent = "";
	public String mType = "";
	public String mAuthor = "";
	public String mNewsId = "";
	public InfoDetailJson() {
		// TODO Auto-generated constructor stub
	}

	public InfoDetailJson(Parcel source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public InfoDetailJson(String url) {
		super(url);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parseJsonData(String result) {
		// TODO Auto-generated method stub
		if(result == null)
		{
			return;
		}
		try {
			JSONObject obj = JSON.parseObject(result);
			mCode = obj.getIntValue("code");
			mMsg = obj.getString("msg");
			
			JSONArray arr = obj.getJSONArray("data");
			
			if(arr.size() > 0)
			{
				JSONObject js = arr.getJSONObject(0);
				mTitle = js.getString("title");
				if(js.containsKey("type"))
				{
					mType = js.getString("type");
				}
				if (js.containsKey("author")) {
					mAuthor = js.getString("author");
				}
				if (js.containsKey("newsid")) {
					mNewsId = js.getString("newsid");
				}
				if (js.containsKey("content")) {
					mContent = js.getString("content");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
