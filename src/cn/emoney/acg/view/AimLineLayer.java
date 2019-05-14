package cn.emoney.acg.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import cn.emoney.sky.libs.chart.layers.ChartLayer;

public class AimLineLayer extends ChartLayer {

	private Context mContext = null;
	PointF mAimPointf = null;

	private float mStrokeWidth = 1;
	private boolean mIsShowHLine = false;
	private boolean mIsShowVLine = false;
	private boolean mIsAimOn = false;
	private float mAimPointRadius = 0;

	public AimLineLayer() {
	}

	@Override
	public RectF prepareBeforeDraw(RectF rect) {
		mLeft = rect.left;
		mRight = rect.right;
		mTop = rect.top;
		mBottom = rect.bottom;

		getPaint().setColor(mColor);
		getPaint().setStrokeWidth(mStrokeWidth);
		getPaint().setAntiAlias(true);
		getPaint().setStyle(Style.FILL);

		return new RectF(mLeft, mTop, mRight, mBottom);
	}

	@Override
	public void doDraw(Canvas canvas) {

		if (!mIsAimOn || mAimPointf == null) {
			return;
		}

		getPaint().setColor(mColor);
		getPaint().setStrokeWidth(mStrokeWidth);

		if (mIsShowHLine) {
			if (mAimPointf.y > mTop && mAimPointf.y < mBottom) {
				canvas.drawLine(mLeft, mAimPointf.y, mRight, mAimPointf.y, getPaint());
			}
		}

		if (mIsShowVLine) {
			if (mAimPointf.x > mLeft && mAimPointf.x < mRight) {
				canvas.drawLine(mAimPointf.x, mTop, mAimPointf.x, mBottom, getPaint());
			}
		}

		if (mAimPointRadius > 0) {
			if (mAimPointf.y > mTop && mAimPointf.y < mBottom && mAimPointf.x > mLeft && mAimPointf.x < mRight) {
				canvas.drawCircle(mAimPointf.x, mAimPointf.y, mAimPointRadius, getPaint());
			}
		}
	}

	@Override
	public void rePrepareWhenDrawing(RectF rect) {

	}

	public void setStrokeWidth(int width)
	{
		mStrokeWidth = width;
	}
	
	public void setAimPointf(PointF pointf) {
		mAimPointf = pointf;
	}

	public void setIsShowHLine(boolean b) {
		mIsShowHLine = b;
	}

	public void setIsShowVLine(boolean b) {
		mIsShowVLine = b;
	}

	public void switchAim(boolean isOn) {
		mIsAimOn = isOn;
	}

	public void setAimPointRadius(float r) {
		mAimPointRadius = r;
	}
}
