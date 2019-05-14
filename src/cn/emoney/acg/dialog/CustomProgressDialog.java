package cn.emoney.acg.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import cn.emoney.acg.R;

/**
 * @ClassName: CustomProgressDialog
 * @Description: 可以被取消的加载进度对话框
 * @author xiechengfa
 * @date 2015年11月13日 下午2:08:46
 *
 */
public class CustomProgressDialog extends ProgressDialog {
    private String info = null;
    private TextView infoView = null;
    private OnCancelProgressDiaListener listener = null;

    public CustomProgressDialog(Context context) {
        super(context, R.style.AlertDialogIOSStyle);
    }

    public CustomProgressDialog(Context context, boolean isShowShadow) {
        super(context, isShowShadow ? R.style.AlertDialogIOSStyle : R.style.AlertDialogIOSStyle_noshadow);

    }

    public void setOnCancelProgressDiaListener(OnCancelProgressDiaListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_progress_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        if (info != null && info.trim().length() > 0) {
            infoView = (TextView) findViewById(R.id.infoView);
            infoView.setText(info);
        }
    }

    public void setInfo(String info) {
        this.info = info;
        if (info != null && info.trim().length() > 0 && infoView != null) {
            infoView.setText(info);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (listener != null) {
                listener.onCancelProgressDia();
                dismiss();
            }
        }
        return true;
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
        super.show();
    }

    /**
     * 关闭进度对话框回调
     * 
     * @author jason.ye
     * 
     */
    public interface OnCancelProgressDiaListener {
        /**
         * 关闭对话框
         */
        public void onCancelProgressDia();
    }
}
