package cn.emoney.acg.helper.thirdpartylogin;

import android.content.Context;

public class ThirdPartyLogin {
	private static ThirdPartyLogin mInstance = null;
	
	private QQAuth mQqAuth_instance = null;
	private SinaWeiBoAuth mWeiboAuth_instance = null;
	
	public static ThirdPartyLogin getInstance()
	{
		if (mInstance == null) {
			mInstance = new ThirdPartyLogin();
		}
		
		return mInstance;
	}
	
	public QQAuth getQQAuth()
	{
		return mQqAuth_instance;
	}
	
	public QQAuth getQQAuth(Context context)
	{
		if (mQqAuth_instance == null) {
			mQqAuth_instance = new QQAuth(context);
		}
		
		return mQqAuth_instance;
	}
	
	public SinaWeiBoAuth getWeiBoAuth(Context context)
	{
		if (mWeiboAuth_instance == null) {
			mWeiboAuth_instance = new SinaWeiBoAuth(context);
		}
		
		return mWeiboAuth_instance;
	}
	
}
