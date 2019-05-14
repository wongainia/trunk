package cn.emoney.acg.helper.thirdpartylogin;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class QQAuth {
	public static final String APP_ID = "101132745";
	
	public Tencent mTencent = null;
	public QQAuListener mListener = null;
	private Context mContext = null;

	private ThirdPartyLoginListener callBack = null;
	QQAuth(Context context) {
		mContext = context;
	}
	
	public void login(Activity activity, ThirdPartyLoginListener callback) {
		this.callBack = callback;
		if (mListener == null) {
			mListener = new QQAuListener();
		}
		
		if (mTencent == null) {
			mTencent = Tencent.createInstance(APP_ID, mContext);
		}
		
		mTencent.login(activity, "get_simple_userinfo", mListener);
		
	}
	
	public void notifyActResult(int requestCode, int resultCode, Intent data) {
		mTencent.onActivityResult(requestCode, resultCode, data);
	}
	
	// qqlogin listener
	private class QQAuListener implements IUiListener {

		@Override
		public void onCancel() {
			if (callBack != null) {
				callBack.onFail(-2);//取消
			}
		}

		@Override
		public void onComplete(Object obj) {
			// {
			// "ret": 0,
			// "pay_token": "6DBBC7F5824941AAEF55DD964B5A4968",
			// "pf": "desktop_m_qq-10000144-android-2002-",
			// "query_authority_cost": 174,
			// "authority_cost": 13315,
			// "openid": "160EFCFFBEB08AD64CDF873CF351BAD5",
			// "expires_in": 7776000,
			// "pfkey": "417fa6043291ec7a8bae5d595257a0d5",
			// "msg": "",
			// "access_token": "CA57D56458D90709B19799B13024AF35",
			// "login_cost": 12755
			// }
			mTencent.logout(mContext);
			org.json.JSONObject jsonObject = (org.json.JSONObject) obj;
			String strJson = jsonObject.toString();
			
			JSONObject fstJObj = JSON.parseObject(strJson);
			JSONObject retObj = new JSONObject();
			try {
				retObj.put(ThirdPartyLoginListener.LOGIN_RET_OPENID, fstJObj.get("openid"));
				retObj.put(ThirdPartyLoginListener.LOGIN_RET_TOKEN, fstJObj.get("access_token"));
				if (callBack != null) {
					callBack.onComplete(retObj);//取消
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onError(UiError arg0) {
			if (callBack != null) {
				callBack.onFail(-1);//取消
			}
		}
	}
}
