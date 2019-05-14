package cn.emoney.acg.view;

import android.view.View;
import android.view.View.OnClickListener;
import cn.emoney.acg.util.ClickEffectUtil;

public abstract class OnClickEffectiveListener implements OnClickListener {
    public void onClick(View v) {
        // 通过上次点击时间跟本次点击时间的时间差来判断是否是有效点击
        if (ClickEffectUtil.isEffectiveClick()) {
            onClickEffective(v);
        }
    }

    public abstract void onClickEffective(View v);
}
