/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package cn.emoney.acg.util.textviewlink;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.View;

public class TouchableSpan extends ClickableSpan {

	private final Link link;
	public boolean touched = false;

	private int textColor;

	/**
	 * Construct new TouchableSpan using the link
	 * 
	 * @param link
	 */
	public TouchableSpan(Context context, Link link) {
		this.link = link;

		if (link.getTextColor() == 0) {
			this.textColor = getDefaultColor(context);
		} else {
			this.textColor = link.getTextColor();
		}
	}

	/**
	 * Finds the default color for links based on the current theme.
	 * 
	 * @param context
	 *            activity
	 * @return color as an integer
	 */
	private int getDefaultColor(Context context) {

		return Color.BLUE;
	}

	/**
	 * This TouchableSpan has been clicked.
	 * 
	 * @param widget
	 *            TextView containing the touchable span
	 */
	public void onClick(View widget) {

		// handle the click
		if (link.getClickListener() != null) {
			link.getClickListener().onClick(link.getText());
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				TouchableMovementMethod.touched = false;
			}
		}, 500);
	}

	/**
	 * This TouchableSpan has been long clicked.
	 * 
	 * @param widget
	 *            TextView containing the touchable span
	 */
	public void onLongClick(View widget) {

		// handle the long click
		if (link.getLongClickListener() != null) {
			link.getLongClickListener().onLongClick(link.getText());
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				TouchableMovementMethod.touched = false;
			}
		}, 500);
	}

	/**
	 * Set the alpha for the color based on the alpha factor
	 * 
	 * @param color
	 *            original color
	 * @param factor
	 *            how much we want to scale the alpha to
	 * @return new color with scaled alpha
	 */
	public int adjustAlpha(int color, float factor) {
		int alpha = Math.round(Color.alpha(color) * factor);
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}

	/**
	 * Draw the links background and set whether or not we want it to be
	 * underlined
	 * 
	 * @param ds
	 *            the link
	 */
	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);

		ds.setUnderlineText(link.isUnderlined());
		ds.setColor(textColor);
		ds.bgColor = touched ? adjustAlpha(textColor, link.getHighlightAlpha()) : Color.TRANSPARENT;
		ds.setTypeface(link.getTypeface());

	}

	/**
	 * Specifiy whether or not the link is currently touched
	 * 
	 * @param touched
	 */
	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	protected static TypedArray obtainStyledAttrsFromThemeAttr(Context context, int themeAttr, int[] styleAttrs) {
		// Need to get resource id of style pointed to from the theme attr
		TypedValue outValue = new TypedValue();
		context.getTheme().resolveAttribute(themeAttr, outValue, true);
		final int styleResId = outValue.resourceId;

		// Now return the values (from styleAttrs) from the style
		return context.obtainStyledAttributes(styleResId, styleAttrs);
	}
}