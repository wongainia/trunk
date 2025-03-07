package cn.emoney.acg.helper.thirdpartylogin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;

public class SinaWeiBoAuth {
	/** 当前 DEMO 应用的 APP_KEY，第三方应用应该使用自己的 APP_KEY 替换该 APP_KEY */
	public static final String APP_KEY = "918479405";

	/**
	 * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
	 * 
	 * <p>
	 * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响， 但是没有定义将无法使用 SDK 认证登录。
	 * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
	 * </p>
	 */
	public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";

	/**
	 * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
	 * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利 选择赋予应用的功能。
	 * 
	 * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的 使用权限，高级权限需要进行申请。
	 * 
	 * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
	 * 
	 * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
	 * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
	 */
	public static final String SCOPE = "";

	public WeiboAuth mWeiboAuth = null;
	public WeiBoAuListener mListener = null;
	private Context mContext = null;

	private ThirdPartyLoginListener callBack = null;

	SinaWeiBoAuth(Context context) {
		mContext = context;
	}

	public void login(Activity activity, ThirdPartyLoginListener callback) {
		this.callBack = callback;
		if (mListener == null) {
			mListener = new WeiBoAuListener();
		}

		if (mWeiboAuth == null) {
			mWeiboAuth = new WeiboAuth(mContext, APP_KEY, REDIRECT_URL, SCOPE);
		}

		mWeiboAuth.anthorize(mListener);

	}

	// public void notifyActResult(int requestCode, int resultCode, Intent data)
	// {
	// mWeiboAuth.onActivityResult(requestCode, resultCode, data);
	// }

	// weibo login listener
	class WeiBoAuListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			if (callBack != null) {
				callBack.onFail(-2);// 取消
			}
		}

		@Override
		public void onComplete(Bundle arg0) {
			Oauth2AccessToken mToken = Oauth2AccessToken.parseAccessToken(arg0);
			String sToken = mToken.getToken();
			String sUid = mToken.getUid();
//			Log.v("sky", "token = " + sToken + "; Uid = " + sUid);
			JSONObject retObj = new JSONObject();
			try {
				retObj.put(ThirdPartyLoginListener.LOGIN_RET_OPENID, sUid);
				retObj.put(ThirdPartyLoginListener.LOGIN_RET_TOKEN, sToken);
				if (callBack != null) {
					callBack.onComplete(retObj);// 取消
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			if (callBack != null) {
				callBack.onFail(-1);// 取消
			}
		}

	}
}
