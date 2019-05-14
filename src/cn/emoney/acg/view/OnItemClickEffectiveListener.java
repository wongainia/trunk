package cn.emoney.acg.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import cn.emoney.acg.util.ClickEffectUtil;

public abstract class OnItemClickEffectiveListener implements OnItemClickListener {
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// 通过上次点击时间跟本次点击时间的时间差来判断是否是有效点击
		if (ClickEffectUtil.isEffectiveClick()) {
			onClickEffective(parent, view, position, id);
		}
	}

	public abstract void onClickEffective(AdapterView<?> parent, View view, int position, long id);

}
