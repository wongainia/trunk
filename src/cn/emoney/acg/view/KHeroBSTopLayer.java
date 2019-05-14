package cn.emoney.acg.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import cn.emoney.acg.R;
import cn.emoney.acg.data.FixPair;
import cn.emoney.acg.data.protocol.quote.CpxReply.Cpx_Reply.cpx_item;

public class KHeroBSTopLayer extends BSTopLayer {

    private Paint mPaint_bs_bg = null;
    private int BS_PADDING_BOTTOM = 2;
    private int mBSAreaBGColor = 0xA6233B5B;

    private List<FixPair<Integer, Float>> m_lstCpxBg_x = new ArrayList<FixPair<Integer, Float>>();

    public KHeroBSTopLayer(Context context) {
        super(context);

        mPaint_bs_bg = new Paint();
        // 填充
        mPaint_bs_bg.setStyle(Paint.Style.FILL);
        // 消除锯齿
        mPaint_bs_bg.setAntiAlias(true);
        // 设置paint的外框宽度
        mPaint_bs_bg.setColor(mBSAreaBGColor);
    }

    public void setBSAreaColor(int bsColor) {
        mBSAreaBGColor = bsColor;
        mPaint_bs_bg.setColor(mBSAreaBGColor);
    }

    public int getBSAreaColor() {
        return mBSAreaBGColor;
    }

    @Override
    public RectF prepareBeforeDraw(RectF rect) {
        // TODO Auto-generated method stub
        RectF rectf = super.prepareBeforeDraw(rect);
        mBt_b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img_khero_bspoint_b);
        mBt_s = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img_bspoint_s);

        return rectf;
    }

    @Override
    public void doDraw(Canvas canvas) {
        m_lstCpxBg_x.clear();

        for (int i = 0; i < mLstBSItems.size(); i++) {
            cpx_item bs_item = mLstBSItems.get(i);
            String bs_flag = bs_item.getBsFlag();
            int date_bs = bs_item.getDatetime();

            float x1 = mLeft;
            int iFlag_BS = 0;
            if (mMapKLineData.containsKey(date_bs)) {
                List<Object> lstItem = mMapKLineData.get(date_bs);
                x1 = (Float) lstItem.get(0);

                if ("1".equals(bs_flag)) {
                    iFlag_BS = 1;
                    // canvas.drawBitmap(mBt_b, x1 - mHalf_width, mBottom -
                    // mImg_height - BS_PADDING_BOTTOM, getPaint());
                } else if ("-1".equals(bs_flag)) {
                    iFlag_BS = -1;
                    // canvas.drawBitmap(mBt_s, x1 - mHalf_width, mBottom -
                    // mImg_height - BS_PADDING_BOTTOM, getPaint());
                }
            }

            FixPair<Integer, Float> t_b = new FixPair<Integer, Float>(iFlag_BS, x1);
            m_lstCpxBg_x.add(t_b);

            // if ("1".equals(bs_flag)) {
            // FixPair<Integer, Float> t_b = new FixPair<Integer, Float>(1, x1);
            // m_lstCpxBg_x.add(t_b);
            // } else if ("-1".equals(bs_flag)) {
            // FixPair<Integer, Float> t_s = new FixPair<Integer, Float>(-1,
            // x1);
            // m_lstCpxBg_x.add(t_s);
            // }

        }

        int lenght = m_lstCpxBg_x.size();
        if (lenght > 0) {
            if (lenght % 2 == 1) {
                FixPair<Integer, Float> t_right = new FixPair<Integer, Float>(0, mRight);
                m_lstCpxBg_x.add(t_right);
            }
            lenght = m_lstCpxBg_x.size();
        }

        for (int i = 0; i < lenght; i += 2) {
            if (lenght - i >= 2) {

                float x1 = m_lstCpxBg_x.get(i).second;
                float x2 = m_lstCpxBg_x.get(i + 1).second;
                if (x1 != x2) {
                    if (x1 > mLeft) {
                        x1 = x1 - mColumWidth / 2;
                    }
                    x2 = x2 + mColumWidth / 2;
                    canvas.drawRect(x1, mTop, x2, mBottom, mPaint_bs_bg);
                }

                Bitmap bsBitmap = getBSBitmapByFlag(m_lstCpxBg_x.get(i).first);
                if (bsBitmap != null) {
                    canvas.drawBitmap(bsBitmap, x1 - mHalf_width, mBottom - mImg_height - BS_PADDING_BOTTOM, getPaint());
                }

                bsBitmap = getBSBitmapByFlag(m_lstCpxBg_x.get(i + 1).first);
                if (bsBitmap != null) {
                    canvas.drawBitmap(bsBitmap, x2 - mHalf_width, mBottom - mImg_height - BS_PADDING_BOTTOM, getPaint());
                }
            }
        }

    }

}
