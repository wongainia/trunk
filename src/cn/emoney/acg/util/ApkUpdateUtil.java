package cn.emoney.acg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.util.DownloadFileThread.DownloadFileCallBack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class ApkUpdateUtil {
	private static final String CHECK_UPDATE_URL_FORMAT = "http://down3.emstock.com.cn/istock/phone_files/release/%s/phone_app_info.dat?t=%d";

	public static final String KEY_VER = "ver";
	public static final String KEY_URL = "uri";
	public static final String KEY_UPDATE_INFO = "update_info";

	private String apkPath;
	private String apkDownloadUrl;

	private boolean hasNewVersion = false;
	private boolean cancelUpdate = false;

	private Context mContext = null;

	public ApkUpdateUtil(Context context) {
		this.mContext = context;
	}

	public static interface ApkUpdateUtil_CheckCallBack {
		void onCheckUpdate(int state, JSONObject obj);
	}

	public static interface ApkUpdateUtil_UpdateCallBack {
		void onDownloadProcess(int process);

		void onDownloadSuccess(String path);

		void onFail();
	}

	// 请先check再update
	public void checkNewVersion(String chanel, final String curVersion, final ApkUpdateUtil_CheckCallBack callBack) {

		String url = String.format(CHECK_UPDATE_URL_FORMAT, chanel, DateUtils.getTimeStamp());
		new DownloadFileThread(url, "phone_app_info.dat", new DownloadFileThread.DownloadFileCallBack() {

			@Override
			public void onSuccess(String path) {
				// TODO Auto-generated method stub
				try {
					File file = new File(path);
					FileInputStream fis = new FileInputStream(file);
					if (fis == null) {
						callBack.onCheckUpdate(-1, null); // 检查出错
					}
					int t_len = fis.available();
					byte[] bytes = new byte[t_len];
					fis.read(bytes);

					String strJson = EncodingUtils.getString(bytes, "UTF-8");
					fis.close();
					file.delete();
					if (strJson == null) {
						callBack.onCheckUpdate(-1, null); // 检查出错
					}

					JSONObject jsonObject = JSON.parseObject(strJson);

					String newVersion = jsonObject.getString(KEY_VER);
					int nRet = DataUtils.compareVersion(curVersion, newVersion);
					if (nRet == -1) {
						apkDownloadUrl = jsonObject.getString(KEY_URL);
						callBack.onCheckUpdate(1, jsonObject); // 有更新的版本
					} else {
						callBack.onCheckUpdate(0, null); // 无更新的版本
					}

				} catch (FileNotFoundException e) {
					callBack.onCheckUpdate(-1, null); // 检查出错
					e.printStackTrace();
				} catch (IOException e) {
					callBack.onCheckUpdate(-1, null); // 检查出错
					e.printStackTrace();
				} catch (JSONException e) {
					callBack.onCheckUpdate(-1, null); // 检查出错
					e.printStackTrace();
				}
			}

			@Override
			public void onProcess(int process) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFail() {
				// TODO Auto-generated method stub
				callBack.onCheckUpdate(-1, null); // 检查出错
			}

            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                
            }
		}).start();
	}

	public static String checkIsDownloaded(String apkDownloadUrl) {
		String[] aryStr = apkDownloadUrl.split("/");
		String apkPath = "";

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String t_path = Environment.getExternalStorageDirectory() + "/";
			apkPath = t_path + DataModule.G_LOC_PATH + "download" + "/" + aryStr[aryStr.length - 1];
		}

		File apkFile = new File(apkPath);
		if (apkFile.exists()) {
			return apkPath;
		}
		return "";
	}

	// 请先check再update
	public void downloadApk(final ApkUpdateUtil_UpdateCallBack callBack) {
		String[] aryStr = apkDownloadUrl.split("/");
		new DownloadFileThread(apkDownloadUrl, aryStr[aryStr.length - 1], new DownloadFileCallBack() {

			@Override
			public void onSuccess(String path) {
				if (callBack != null) {
					callBack.onDownloadSuccess(path);
				}

			}

			@Override
			public void onProcess(int process) {
				if (callBack != null) {
					callBack.onDownloadProcess(process);
				}
			}

			@Override
			public void onFail() {
				if (callBack != null) {
					callBack.onFail();
				}
			}

            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                
            }
		}).start();
	}

	public void cancelUpdate() {
		cancelUpdate = true;
	}

	public static int installApkFromFile(Context context, String apkPath) {

		File apkFile = new File(apkPath);
		if (!apkFile.exists()) {
			return -1;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
		context.startActivity(intent);
		return 0;
	}

}
