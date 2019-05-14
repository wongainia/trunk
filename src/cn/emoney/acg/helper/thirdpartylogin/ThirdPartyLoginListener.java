package cn.emoney.acg.helper.thirdpartylogin;

import com.alibaba.fastjson.JSONObject;


public interface ThirdPartyLoginListener {
	public static final String LOGIN_RET_TOKEN = "login_ret_token";
	public static final String LOGIN_RET_OPENID = "login_ret_openid";
	
	public void onComplete(JSONObject object);
	public void onFail(int errCode);
}
