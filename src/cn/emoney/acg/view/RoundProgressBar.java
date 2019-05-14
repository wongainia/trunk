package cn.emoney.acg.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import cn.emoney.acg.R;
import cn.emoney.acg.util.Util;

/**
 * @ClassName: RoundProgressBar
 * @Description:老师端问题语音播放的进度
 * @author xiechengfa
 * @date 2015年12月22日 下午4:24:31
 *
 */
public class RoundProgressBar extends View {
    private final int ROUND_PROGRESS_DEFAULT_COLOR = 0xff4690EF;// 环进度的默认颜色
    // 圆环进度的颜色
    private int roundProgressColor;
    // 圆环进度的宽度
    private float roundProgressWidth;
    // 最大进度
    private int max = 100;
    // 当前进度
    private int progress = 0;
    // 画笔对象的引用
    private Paint paint;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);

        // 获取自定义属性和默认值
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, ROUND_PROGRESS_DEFAULT_COLOR);
        roundProgressWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundProgressWidthOfBar, Util.getResourcesDimension(R.dimen.quiz_teacher_replay_progress_width));
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);

        mTypedArray.recycle();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画圆环的进度
        int centre = getWidth() / 2; // 获取圆心的x坐标
        int radius = (int) (centre - roundProgressWidth / 2); // 圆环进度的半径
        paint.setStrokeWidth(roundProgressWidth); // 设置圆环的宽度
        paint.setColor(roundProgressColor); // 设置进度的颜色
        paint.setStyle(Paint.Style.STROKE); // 设置空心
        paint.setAntiAlias(true); // 消除锯齿

        RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限
        canvas.drawArc(oval, 270, 360 * progress / max, false, paint); // 根据进度画圆弧
    }

    public int getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     * 
     * @param max
     */
    public void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度.需要同步
     * 
     * @return
     */
    public int getProgress() {
        return progress;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
     * 
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }

        if (progress > max) {
            progress = max;
        }

        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }

    public int getRoundProgressColor() {
        return roundProgressColor;
    }

    public void setRoundProgressColor(int roundProgressColor) {
        this.roundProgressColor = roundProgressColor;
    }

    public float getRoundProgressWidth() {
        return roundProgressWidth;
    }

    public void setRoundProgressWidth(float roundProgressWidth) {
        this.roundProgressWidth = roundProgressWidth;
    }
}
