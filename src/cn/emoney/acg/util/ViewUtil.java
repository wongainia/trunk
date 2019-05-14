package cn.emoney.acg.util;

import android.view.View;

public class ViewUtil {
	public static Object getViewTag(View view) {
		if (view == null) {
			return null;
		}

		try {
			return view.getTag();

		} catch (Exception e) {
		}

		return null;

	}
}
