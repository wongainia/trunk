package cn.emoney.acg;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * @ClassName: ACGApplication
 * @Description:全局的Application
 * @author xiechengfa
 * @date 2015年11月9日 下午6:19:43
 *
 */
public class ACGApplication extends Application {

	private static ACGApplication instance = null;

	public static ACGApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		// 初始化语音对象
		// 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用半角“,”分隔。
		// 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

		// 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
		String appid = "56473c33";
		SpeechUtility.createUtility(ACGApplication.this, SpeechConstant.APPID + "=" + appid);

		// 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
		// Setting.setShowLog(false);

		super.onCreate();

		instance = this;
	}
}
