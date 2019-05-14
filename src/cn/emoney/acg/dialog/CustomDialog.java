package cn.emoney.acg.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;

/**
 * @ClassName: CustomDialog
 * @Description:消息对话框
 * @author xiechengfa
 * @date 2015年11月13日 下午1:51:06
 */
public class CustomDialog implements OnClickListener {
    private boolean showTitle = false;
    private boolean showMsg = false;
    private boolean showPosBtn = true;// 默认显示
    private boolean showNegBtn = false;

    private Context context;
    private Dialog dialog;
    // private LinearLayout lLayout_bg;
    private TextView titleView;
    private TextView msgView;
    private Button btnNeg;
    private Button btnPos;
    private ImageView lineView;
    private CustomDialogListener listener = null;

    public CustomDialog(Context context, CustomDialogListener listener) {
        this.context = context;
        this.listener = listener;
        builder();
    }

    public void setCustomDialogListener(CustomDialogListener listener) {
        this.listener = listener;
    }

    private void builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(R.layout.alertdialog_ios_layout, null);

        // 获取自定义Dialog布局中的控件
        LinearLayout lLayout_bg = (LinearLayout) view.findViewById(R.id.lLayout_bg);
        titleView = (TextView) view.findViewById(R.id.txt_title);
        titleView.setVisibility(View.GONE);

        msgView = (TextView) view.findViewById(R.id.txt_msg);
        msgView.setVisibility(View.GONE);

        btnNeg = (Button) view.findViewById(R.id.btn_neg);
        btnNeg.setVisibility(View.GONE);
        btnNeg.setOnClickListener(this);

        btnPos = (Button) view.findViewById(R.id.btn_pos);
        btnPos.setVisibility(View.GONE);
        btnPos.setOnClickListener(this);

        lineView = (ImageView) view.findViewById(R.id.img_line);
        lineView.setVisibility(View.GONE);

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.AlertDialogIOSStyle);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);

        // 调整dialog背景大小
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (DataModule.SCREEN_WIDTH * DataModule.DIALOG_WIDTH_SCALE), LayoutParams.WRAP_CONTENT));
    }

    public CustomDialog setCustomTitle(String title) {
        if (title != null && title.trim().length() > 0) {
            showTitle = true;
            titleView.setText(title);
        }

        return this;
    }

    public CustomDialog setCustomMessage(String msg) {
        if (msg != null && msg.trim().length() > 0) {
            showMsg = true;
            msgView.setText(msg);
        }

        return this;
    }

    public CustomDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);

        // 根据当前的需求的特殊处理，当需求要求不能返回键取消同样也不能点击取消
        if (!cancel) {
            setCanceledOnTouchOutside(false);
        }
        return this;
    }

    public CustomDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public CustomDialog setMessageGravity(int gravity) {
        // 默认是居中
        if (msgView != null) {
            msgView.setGravity(gravity);
        }
        return this;
    }

    public CustomDialog setButtonText(String posBtnText, String negBtnText) {
        if (posBtnText != null && posBtnText.trim().length() > 0) {
            showPosBtn = true;
            btnPos.setText(posBtnText);
        } else {
            showPosBtn = false;
        }

        if (negBtnText != null && negBtnText.trim().length() > 0) {
            showNegBtn = true;
            btnNeg.setText(negBtnText);
        } else {
            showNegBtn = false;
        }

        return this;
    }

    public CustomDialog setTwoButtonDefaultText() {
        showPosBtn = true;
        showNegBtn = true;
        return this;
    }

    public CustomDialog setOneButtonDefaultText() {
        showPosBtn = true;
        showNegBtn = false;
        return this;
    }

    private void setLayout() {
        if (showTitle) {
            titleView.setVisibility(View.VISIBLE);
        }

        if (showMsg) {
            msgView.setVisibility(View.VISIBLE);
        }

        if (showPosBtn && showNegBtn) {
            btnPos.setVisibility(View.VISIBLE);
            btnNeg.setVisibility(View.VISIBLE);
            lineView.setVisibility(View.VISIBLE);
            btnNeg.setBackgroundResource(R.drawable.selector_alertdialog_right);
            btnPos.setBackgroundResource(R.drawable.selector_alertdialog_left);
        } else if (showPosBtn && !showNegBtn) {
            btnPos.setVisibility(View.VISIBLE);
            btnPos.setBackgroundResource(R.drawable.selector_single_alertdialog);
        } else if (!showPosBtn && showNegBtn) {
            btnNeg.setVisibility(View.VISIBLE);
            btnNeg.setBackgroundResource(R.drawable.selector_single_alertdialog);
        }
    }

    public boolean isShowing() {
        if (dialog != null) {
            return dialog.isShowing();
        }
        return false;
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void show() {
        setLayout();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_neg) {
            if (listener != null) {
                listener.onCancelBtnClicked();
            }
        } else if (v.getId() == R.id.btn_pos) {
            if (listener != null) {
                listener.onConfirmBtnClicked();
            }
        }

        dialog.dismiss();
    }

    public interface CustomDialogListener {
        /**
         * 确认
         */
        public void onConfirmBtnClicked();

        /**
         * 取消
         */
        public void onCancelBtnClicked();
    }
}
