package cn.emoney.acg.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.util.Util;

/**
 * @类说明:列表对话框
 * @作者:xiechengfa
 * @创建时间:2014-10-23 下午5:10:10
 */
public class ListDialog {
    private boolean showTitle = false;
    private Activity context;
    private Dialog dialog;
    private TextView txt_title;
    private TextView txt_cancel;
    private LinearLayout lLayout_content;
    private ScrollView sLayout_content;
    private OnListDialogItemClickListener listener = null;
    private ArrayList<ListDialogMenuInfo> sheetItemList = null;

    public ListDialog(Activity context, OnListDialogItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        builder();
    }

    public void setOnListDialogItemClickListener(OnListDialogItemClickListener listener) {
        this.listener = listener;
    }

    private void builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(R.layout.listdialog_layout, null);

        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);

        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(DataModule.SCREEN_WIDTH);

        // 获取自定义Dialog布局中的控件
        sLayout_content = (ScrollView) view.findViewById(R.id.sLayout_content);
        lLayout_content = (LinearLayout) view.findViewById(R.id.lLayout_content);
        txt_title = (TextView) view.findViewById(R.id.txt_title);
        txt_cancel = (TextView) view.findViewById(R.id.txt_cancel);
        txt_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.ListDialogIOSStyle);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
    }

    public ListDialog setTitleMessage(String title) {
        if (title != null && title.trim().length() > 0) {
            showTitle = true;
            txt_title.setVisibility(View.VISIBLE);
            txt_title.setText(title);
        }
        return this;
    }

    public ListDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public ListDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    /**
     * 
     * @param strItem 条目名称
     * @param listener
     * @return
     */
    public ListDialog setItems(ArrayList<ListDialogMenuInfo> sheetItemList) {
        this.sheetItemList = sheetItemList;
        initListItems();
        return this;
    }

    public void show() {
        dialog.show();
    }

    /** 设置条目布局 */
    private void initListItems() {
        if (sheetItemList == null || sheetItemList.size() <= 0) {
            return;
        }

        lLayout_content.removeAllViews();
        int size = sheetItemList.size();

        // TODO 高度控制，非最佳解决办法
        // 添加条目过多的时候控制高度
        if (size >= 7) {
            LinearLayout.LayoutParams params = (LayoutParams) sLayout_content.getLayoutParams();
            params.height = DataModule.SCREEN_HEIGHT / 2;
            sLayout_content.setLayoutParams(params);
        }

        // 循环添加条目
        for (int i = 0; i < size; i++) {
            final ListDialogMenuInfo sheetItem = sheetItemList.get(i);
            LinearLayout layoutItem = new LinearLayout(context);
            layoutItem.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Util.getResourcesDimension(R.dimen.actionsheet_height)));
            layoutItem.setBackgroundResource(R.drawable.selector_list_item);
            layoutItem.setGravity(Gravity.CENTER);

            if (sheetItem.getIconResId() != 0) {
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(sheetItem.getIconResId());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.rightMargin = Util.getResourcesDimension(R.dimen.actionsheet_icon_text_sp);
                layoutItem.addView(imageView, lp);
            }

            TextView textView = new TextView(context);
            textView.setTextColor(Util.getResourcesColor(R.color.t1));
            textView.setText(sheetItem.getItemName());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Util.getResourcesDimension(R.dimen.txt_s5));
            textView.setGravity(Gravity.CENTER);
            layoutItem.addView(textView);


            // 分隔线
            View lineView = null;
            if (i == 0) {
                if (showTitle) {
                    lineView = new View(context);
                }
            } else {
                if (i >= 1 && i < size) {
                    lineView = new View(context);
                }
            }

            if (lineView != null) {
                lineView.setBackgroundColor(Util.getResourcesColor(R.color.b5));
                lineView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Util.getResourcesDimension(R.dimen.line_h)));
                lLayout_content.addView(lineView);
            }

            // 点击事件
            layoutItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(sheetItem.getItemCode());
                    }
                    dialog.dismiss();
                }
            });

            lLayout_content.addView(layoutItem);
        }
    }

    /**
     * 列表对话框，选项监听器
     * 
     * @author xiechengfa
     * 
     */
    public interface OnListDialogItemClickListener {
        /**
         * 选中列表项
         * 
         * @param position 列表位置
         */
        public void onItemClicked(int code);
    }
}
