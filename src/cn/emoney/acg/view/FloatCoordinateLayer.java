package cn.emoney.acg.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import cn.emoney.sky.libs.chart.layers.ChartLayer;

public class FloatCoordinateLayer extends ChartLayer {

	private float mTextHeight = 0;

	private Align mAlign = Align.LEFT;
	private int mTextSize = 18;
	private float mTextWidth = 0;

	private TextAtom mTextAtom = null;

	private boolean mIsSwitchOn = false;

	private float mAxisAreaWidth = 0;

	public RectF prepareBeforeDrawFixed(RectF rect) {
		// TODO Auto-generated method stub
		getPaint().setColor(mColor);
		getPaint().setTextSize(mTextSize);
		getPaint().setAntiAlias(true);
		getPaint().setTextAlign(Align.LEFT);

		mLeft = rect.left;
		mTop = rect.top;
		mBottom = rect.bottom;

		mRight = mLeft + mAxisAreaWidth + mPaddingLeft + mPaddingRight;

		return new RectF(mLeft, mTop, mRight, mBottom);
	}

	@Override
	public void doDraw(Canvas canvas) {
		if (!mIsSwitchOn && mTextAtom == null) {
			return;
		}

		if (mTextAtom.getCoorDinateY() < mTop || mTextAtom.getCoorDinateY() > mBottom) {
			return;
		}

		getPaint().setColor(mColor);

		Paint.FontMetrics fm = getPaint().getFontMetrics();

		TextAtom text = mTextAtom;
		mTextWidth = getPaint().measureText(text.getText());

		if (mAlign == Align.LEFT) {
			float startX = mLeft + mPaddingLeft;
			// getPaint().setColor(text.getTextColor());
			canvas.drawText(text.getText(), startX, text.getCoorDinateY(), getPaint());
		} else if (mAlign == Align.CENTER) {
			float startX = mLeft + mPaddingLeft + (mRight - mLeft - mPaddingLeft - mPaddingRight - mTextWidth) / 2;
			// getPaint().setColor(text.getTextColor());
			canvas.drawText(text.getText(), startX, text.getCoorDinateY(), getPaint());
		} else if (mAlign == Align.RIGHT) {
			float startX = mRight - mPaddingRight - mTextWidth;
			// getPaint().setColor(text.getTextColor());
			canvas.drawText(text.getText(), startX, text.getCoorDinateY(), getPaint());
		}
	}

	@Override
	public void rePrepareWhenDrawing(RectF rect) {
		// TODO Auto-generated method stub

	}

	public void setAlign(Align align) {
		mAlign = align;
	}

	public void setTextSize(int textSize) {
		mTextSize = textSize;
	}

	public void clearText() {
		mTextAtom = null;
	}

	public void setAxisAreaWidth(float width) {
		mAxisAreaWidth = width;
	}

	public void setText(TextAtom ta) {
		mTextAtom = ta;
	}

	public void switchOn(boolean isOn) {
		mIsSwitchOn = isOn;
	}

	public static interface OnFormatDataListener {
		public String onFormatData(float val);
	}

	private OnFormatDataListener mFormatDataListener = null;

	public void setOnFormatDataListener(OnFormatDataListener listener) {
		mFormatDataListener = listener;
	}

	public static class TextAtom {
		private String mText = "";
		private int mTextColor = Color.BLACK;
		private float mCoorDinateY = 0;

		public TextAtom(String text, int textColor, float coordinateY) {
			mText = text;
			mTextColor = textColor;
			mCoorDinateY = coordinateY;
		}

		public float getCoorDinateY() {
			return mCoorDinateY;
		}

		public void setCoorDinateY(float coorDinateY) {
			this.mCoorDinateY = coorDinateY;
		}

		public int getTextColor() {
			return mTextColor;
		}

		public void setTextColor(int color) {
			mTextColor = color;
		}

		public String getText() {
			return mText;
		}

		public void setText(String text) {
			mText = text;
		}

	}

	public RectF prepareBeforeDraw(RectF rect) {
		// TODO Auto-generated method stub
		getPaint().setColor(mColor);
		getPaint().setTextSize(mTextSize);
		getPaint().setAntiAlias(true);
		getPaint().setTextAlign(Align.LEFT);

		mLeft = rect.left;
		mTop = rect.top;
		mBottom = rect.bottom;

		mRight = mLeft + mAxisAreaWidth + mPaddingLeft + mPaddingRight;

		return new RectF(mLeft, mTop, mRight, mBottom);
	}

}
