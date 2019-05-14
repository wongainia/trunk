package cn.emoney.acg.module;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.module.Module;
import cn.emoney.sky.libs.page.PageIntent;

public class IndependentModuleHome extends Module {
	public static final String KEY_TARGET_CLASS = "TARGET_CLASS";
	private Bundle mBundle = new Bundle();

	private Class<? extends PageImpl> mTargetClass = null;

	@Override
	public void receiveData(Intent intent) {

		super.receiveData(intent);
		if (intent != null) {
			this.mBundle = intent.getExtras();
			if (mBundle != null && mBundle.containsKey(KEY_TARGET_CLASS)) {
				try {
					mTargetClass = (Class<? extends PageImpl>) Class.forName(mBundle.getString(KEY_TARGET_CLASS));
					LogUtil.easylog("mTargetClass:" + mTargetClass);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void initData() {

	}

	@Override
	public void initModule() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.module_independenthome);

		if (mTargetClass == null) {
			finish();
			return;
		}

		PageIntent intent = new PageIntent(null, mTargetClass);
		
		
		intent.setArguments(mBundle);
		intent.setSupportAnimation(false);
		startPage(DataModule.G_CURRENT_FRAME, intent);
	}

}
