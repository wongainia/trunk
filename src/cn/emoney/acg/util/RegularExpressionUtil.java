package cn.emoney.acg.util;

public class RegularExpressionUtil {
	// 正则判断是否为邮箱
	public static boolean isEmail(String s) {
		String regString = "[A-Z0-9a-z._]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,5}";
		boolean b = s.matches(regString);
		return b;
	}

	// 正则判断是否为手机号
	public static boolean isMobilePhoneNum(String s) {
		String regString = "^[1][3-8]\\d{9}";
		boolean b = s.matches(regString);
		return b;
	}

	// 判断是否全为数字组成
	public static boolean isOnlyDigital(String s) {
		String regString = "\\d+";
		boolean b = s.matches(regString);

		return b;
	}

	
	// 判断是否全由数字和字母组成
	public static boolean isDigitalAndLetter(String s) {
		String regString = "[0-9a-zA-Z]+";
		boolean b = s.matches(regString);

		return b;
	}
}
