package cn.emoney.acg.util;

public class ClickEffectUtil {

	private static long lastClickTime = 0;

	// 1000内点击无效
	public static boolean isEffectiveClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;

		if (timeD < 1000) {
			return false;
		}
		lastClickTime = time;
		return true;
	}

	public static boolean isDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (timeD < 300) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
}
