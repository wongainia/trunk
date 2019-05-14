package cn.emoney.acg.util;

import android.content.Context;

public class FontUtils {

	public final static int COLOR_RISE = 1;
	public final static int COLOR_FALL = -1;
	public final static int COLOR_EQUAL = 0;
	public final static int SIZE_TXT_TITLEBAR = 20;
	public final static int SIZE_TXT_MENUBAR = 13;
	public final static int SIZE_TXT_SELECTBAR = 17;
	
	public final static int SIZE_TXT_QUOTE_PRICE = 34;
	public final static int SIZE_TXT_QUOTE_ZDF = 15;
	public final static int SIZE_TXT_QUOTE_OTHER = 13;
	
	

	private final static String[] KEY_WORDS_RISE = { "银行转证券", "买入", "初始交易", "延期购回", "补充质押", "基金申购" };
	private final static String[] KEY_WORDS_FALL = { "证券转银行", "卖出", "购回交易", "提前购回", "部分解质", "基金赎回" };

	public static int getColorByPrice(float basePrice, float newPrice) {
		int color = COLOR_EQUAL;
		if (newPrice > basePrice) {
			color = COLOR_RISE;
		} else if (newPrice < basePrice) {
			color = COLOR_FALL;
		}

		return color;
	}

	public static int getColorByZD(float zd) {
		int color = COLOR_EQUAL;
		if (zd > 0) {
			color = COLOR_RISE;
		} else if (zd < 0) {
			color = COLOR_FALL;
		} else {
			color = COLOR_EQUAL;
		}
		return color;
	}

	public static int getColorByZD(String strZd) {
		int color = COLOR_EQUAL;
		try {
			float zd = Float.parseFloat(strZd);
			return getColorByZD(zd);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return color;
	}

	public static int getColorByZDF(float zdf) {
		int color = COLOR_EQUAL;
		if (zdf > 0) {
			color = COLOR_RISE;
		} else if (zdf < 0) {
			color = COLOR_FALL;
		} else {
			color = COLOR_EQUAL;
		}
		return color;
	}

	public static int getColorByZDF(String strZdf) {
		int color = COLOR_EQUAL;
		try {
			float zdf = Float.parseFloat(strZdf);
			return getColorByZDF(zdf);
		} catch (Exception e) {
			return color;
		}

	}

	public static int getColorByZDF_percent(String strZdf) {
		strZdf = strZdf.replaceAll("%", "");
		int color = COLOR_EQUAL;

		try {
			float zdf = Float.parseFloat(strZdf);
			return getColorByZDF(zdf);
		} catch (Exception e) {
			return color;
		}

	}

	public static int getColorBySigned(String str) {
		char flag = 0;
		if (str != null && !str.equals("--")) {
			flag = str.charAt(0);

			if (flag == '+') {
				return COLOR_RISE;
			} else if (flag == '-') {
				return COLOR_FALL;
			} else {
				return COLOR_EQUAL;
			}
		} else {
			return COLOR_EQUAL;
		}
	}

	public static int getColorByKeyWords(String words) {
		for (int i = 0; i < KEY_WORDS_RISE.length; i++) {
			if (words.contains(KEY_WORDS_RISE[i])) {
				return COLOR_RISE;
			}
		}
		for (int i = 0; i < KEY_WORDS_FALL.length; i++) {
			if (words.contains(KEY_WORDS_FALL[i])) {
				return COLOR_FALL;
			}
		}
		return COLOR_EQUAL;
	}

	/**
	 * 将px值转换为dip或dp值，保证尺寸大小不变
	 * 
	 * @param pxValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 * 
	 * @param dipValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * 
	 * @param pxValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * 
	 * @param spValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
