package cn.emoney.acg.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.TextUtils;
import android.graphics.RectF;
import cn.emoney.sky.libs.chart.layers.ChartLayer;

public class PriceLayer extends ChartLayer {

	private int mZDTextSize = 12;
	private int mZDTextColor = Color.BLACK;
	private int mZFTextSize = 12;
	private int mZFTextColor = Color.BLACK;
	private int mPriceTextSize = 24;
	private int mPriceTextColor = Color.BLACK;
	
	private Bitmap mArrowBmp = null;
	private String mZD = "";
	private String mZF = "";
	private String mPrice = "";
	private float mAvailWidth = 0;
	private float mAvailHeight = 0;
	private int mPadding = 8;
	public PriceLayer() {
	}

	@Override
	public RectF prepareBeforeDraw(RectF rect) {
		getPaint().setColor(mColor);
		getPaint().setAntiAlias(true);
		getPaint().setTextAlign(Align.LEFT);
		mLeft = rect.left;
		mRight = rect.right;
		
		mTop = rect.top;
		mBottom = rect.bottom;
		
		mAvailHeight = mBottom - mTop - mPaddingTop - mPaddingBottom;
		mAvailWidth = mRight - mLeft - mPaddingLeft - mPaddingRight;
		return new RectF(mLeft, mTop, mRight, mBottom);
	}

	@Override
	public void doDraw(Canvas canvas) {
	    // draw price
		getPaint().setColor(mPriceTextColor);
		getPaint().setTextSize(mPriceTextSize);
		getPaint().setTextScaleX(0.8f);
		float startX = mLeft + mPaddingLeft;
		Paint.FontMetrics fm = getPaint().getFontMetrics();
		float priceHeight = (float) (Math.ceil(fm.descent - fm.ascent) + 2);
		float startY = mTop +  mAvailHeight/ 2 + mPaddingTop;
		float textWidth = 0f;
		if (TextUtils.isEmpty(mPrice)) {
		    textWidth = getPaint().measureText("—");
		    canvas.drawText("—", startX + mAvailWidth/ 2 - textWidth/2, startY, getPaint());
		} else {
		    textWidth = getPaint().measureText(mPrice);
		    canvas.drawText(mPrice, startX + mAvailWidth/ 2 - textWidth/2, startY, getPaint());
		}
		
		getPaint().setTextScaleX(1);
		startY += (priceHeight/2 + 5);
		
		getPaint().setColor(mZFTextColor);
		getPaint().setTextSize(mZFTextSize);
		float zfWidth = 0f;
		float zdWidth = 0f;
		if (TextUtils.isEmpty(mZF)) {
		    zfWidth = getPaint().measureText("—%");
		} else {
		    zfWidth = getPaint().measureText(mZF);
		}
		if (TextUtils.isEmpty(mZD)) {
		    zdWidth = getPaint().measureText("—");
        } else {
            zdWidth = getPaint().measureText(mZD);
        }
		
		float leftHalf = 0f;
		
		// draw arrow
		int arrowW = 0;
		if (mArrowBmp != null && !TextUtils.isEmpty(mZF)) {
		    arrowW = mArrowBmp.getWidth();
		    leftHalf = (mAvailWidth - arrowW - 5 - zfWidth - 40 - zdWidth) / 2;
		    canvas.drawBitmap(mArrowBmp, startX + leftHalf, startY - mArrowBmp.getHeight() + 1.5f, getPaint());
		} else {
		    leftHalf = (mAvailWidth - zfWidth - 40 - zdWidth) / 2;
		}
		
		// draw adf text
		if (TextUtils.isEmpty(mZF)) {
		    canvas.drawText("—%", startX + leftHalf, startY, getPaint());
		}  else {
		    canvas.drawText(mZF, startX + leftHalf + arrowW + 5, startY, getPaint());
		}
		
		// draw zd text
		getPaint().setColor(mZDTextColor);
		getPaint().setTextSize(mZDTextSize);
		if (TextUtils.isEmpty(mZD)) {
		    canvas.drawText("—", startX + leftHalf + zfWidth + 40, startY, getPaint());
		} else {
		    canvas.drawText(mZD, startX + leftHalf + arrowW + 5 + zfWidth + 40, startY, getPaint());		    
		}
	}

	@Override
	public void rePrepareWhenDrawing(RectF rect) {
	}
	
	//设置涨跌箭头图
	public void setArrowBmp(Bitmap bmp)
	{
		mArrowBmp = bmp;
	}
	
	public void setZDTextSize(int textSize)
	{
		mZDTextSize = textSize;
	}
	public void setZDTextColor(int color)
	{
		mZDTextColor = color;
	}
	
	public void setZFTextSize(int textSize)
	{
		mZFTextSize = textSize;
	}
	public void setZFTextColor(int color)
	{
		mZFTextColor = color;
	}
	
	public void setPriceTextSize(int textSize)
	{
		mPriceTextSize = textSize;
	}
	public void setPriceTextColor(int color)
	{
		mPriceTextColor = color;
	}
	
	public void setZDText(String zd)
	{
		mZD = zd;
	}
	public void setZFText(String zf)
	{
		mZF = zf;
	}
	public void setPriceText(String price)
	{
		mPrice = price;
	}
	
	public void setPadding(int padding)
	{
		mPadding = padding;
	}
}
