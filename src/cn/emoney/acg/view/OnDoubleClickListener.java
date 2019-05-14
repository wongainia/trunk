package cn.emoney.acg.view;

import android.view.View;
import android.view.View.OnClickListener;
import cn.emoney.acg.util.ClickEffectUtil;

public abstract class OnDoubleClickListener implements OnClickListener {
	public void onClick(View v) {
		if (ClickEffectUtil.isDoubleClick()) {
			onDoubleClick(v);
		}
	}

	public abstract void onDoubleClick(View v);

}
