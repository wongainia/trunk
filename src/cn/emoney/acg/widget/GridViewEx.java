package cn.emoney.acg.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * @ClassName: GridViewEx
 * @Description:扩展的GridView,可以响空白行的事件
 * @author xiechengfa
 * @date 2015年11月12日 下午4:49:13
 *
 */
public class GridViewEx extends GridView {
    private OnTouchBlankPositionListener listener = null;

    public GridViewEx(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public GridViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public GridViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }


    public interface OnTouchBlankPositionListener {
        /**
         * 
         * @return 是否要终止事件的路由
         */
        boolean onTouchBlankPosition();
    }

    public void setOnTouchBlankPositionListener(OnTouchBlankPositionListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (listener == null) {
            return super.onTouchEvent(event);
        }

        if (!isEnabled()) {
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return isClickable() || isLongClickable();
        }

        final int motionPosition = pointToPosition((int) event.getX(), (int) event.getY());
        if (motionPosition == INVALID_POSITION) {
            super.onTouchEvent(event);
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                return listener.onTouchBlankPosition();
            } else {
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}
