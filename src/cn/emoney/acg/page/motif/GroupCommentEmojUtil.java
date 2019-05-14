package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupCommentEmojUtil {
	public static final String IMG_TAG_REGULAR = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";

	public static String serverComment2Local(String serverComment) {
		String sRet = serverComment;

		if (serverComment != null && !serverComment.equals("")) {
			Pattern p = Pattern.compile("/[0-9]+\\.png'");
			Matcher m = p.matcher(serverComment);

			List<Integer> tLstPngNumber = new ArrayList<Integer>();
			while (m.find()) {
				String s = m.group();
				int lastIndex = s.indexOf(".");
				if (lastIndex > 1) {
					String sNumber = s.substring(1, lastIndex);
					int num = Integer.parseInt(sNumber);
					tLstPngNumber.add(num);
				}
			}

			for (int i = 0; i < tLstPngNumber.size(); i++) {
				sRet = sRet.replaceFirst(IMG_TAG_REGULAR, new String(Character.toChars(0xfff00 + tLstPngNumber.get(i))));

			}
		}

		return sRet;
	}

	public static String localComment2Server(String localComment) {

		if (localComment != null && !localComment.equals("")) {
			StringBuilder sBuilder = new StringBuilder(localComment);
			for (int i = 0; i < sBuilder.length(); ) {
				int unicode = Character.codePointAt(sBuilder.toString(), i);
				if (unicode >= 0xfff01 && unicode <= 0xfff1e) {
					String ts = "[" + (unicode - 0xfff00) + ']';
					int addLen = ts.length();
					sBuilder.replace(i, i + 2, ts);
					i = i + addLen;
					
				}
				else {
					i++;
				}
			}

			return sBuilder.toString();
		}
		return localComment;
	}

}
