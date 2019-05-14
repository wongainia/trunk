package cn.emoney.acg.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.CustomProgressDialog.OnCancelProgressDiaListener;
import cn.emoney.acg.dialog.ListDialog.OnListDialogItemClickListener;

/**
 * @ClassName: DialogUtils
 * @Description: 对话框和toast的工具类
 * @author xiechengfa
 * @date 2015年11月13日 下午2:09:39
 *
 */
public class DialogUtils {
    /**
     * 显示对话框(一个确定按钮，默认的title，没有事件回调)
     * 
     * @Title: showMessageDialog
     * @Description:
     * @param @param activity
     * @param @param msg
     * @return void
     * @throws
     */
    public static void showMessageDialogOfDefaultSingleBtn(Activity activity, String title, String msg) {
        if (msg == null || msg.trim().length() <= 0) {
            return;
        }

        CustomDialog dialog = new CustomDialog(activity, null);
        dialog.setButtonText("确定", null);
        dialog.setCustomTitle(title);
        dialog.setMessageGravity(Gravity.CENTER);
        dialog.setCustomMessage(msg);
        dialog.show();
    }

    /**
     * 显示对话框(一个确定按钮，默认的title,只能确定关闭,有事件回调)
     * 
     * @Title: showMessageDialog
     * @Description:
     * @param @param activity
     * @param @param msg
     * @return void
     * @throws
     */
    public static void showMessageDialogOfDefaultSingleBtnCallBack(Activity activity, String title, String msg, CustomDialogListener listener) {
        if (msg == null || msg.trim().length() <= 0) {
            return;
        }

        CustomDialog dialog = new CustomDialog(activity, listener);
        dialog.setButtonText("确定", null);
        dialog.setMessageGravity(Gravity.CENTER);
        dialog.setCustomTitle(title);
        dialog.setCustomMessage(msg);
        dialog.show();
    }

    /**
     * 显示对话框
     * 
     * @Title: showMessageDialog
     * @Description:
     * @param @param activity
     * @param @param msg
     * @return void
     * @throws
     */
    public static void showMessageDialog(Activity activity, String title, String msg, String posBtnText, String negBtnText, CustomDialogListener listener) {
        if (msg == null || msg.trim().length() <= 0) {
            return;
        }

        CustomDialog dialog = new CustomDialog(activity, listener);
        dialog.setCustomTitle(title);
        dialog.setCustomMessage(msg);
        dialog.setMessageGravity(Gravity.CENTER);
        dialog.setButtonText(posBtnText, negBtnText);
        dialog.show();
    }


    /**
     * 显示列表对话框
     * 
     * @param activity
     * @param sheetItemList
     * @param listener
     */
    public static void showListDialog(Activity activity, ArrayList<ListDialogMenuInfo> sheetItemList, OnListDialogItemClickListener listener) {
        ListDialog listDialog = new ListDialog(activity, listener);
        listDialog.setItems(sheetItemList);
        listDialog.show();
    }

    private static CustomProgressDialog progressDialog = null;
    private static Context myContext = null;

    /**
     * 显示加载对话框，设置了取消监听器此对话框可以中断，不设置则无法中断
     * 
     * @Title: showProgressDialogOfBase
     * @Description:
     * @param @param activity
     * @param @param listener
     * @return void
     * @throws
     */
    public static void showProgressDialog(Context context, OnCancelProgressDiaListener listener) {
        if (progressDialog == null || context != myContext) {
            myContext = context;

            closeProgressDialog();
            progressDialog = new CustomProgressDialog(context);
        }

        progressDialog.setOnCancelProgressDiaListener(listener);
        if (progressDialog.isShowing()) {
            return;
        }

        progressDialog.show();
    }

    /**
     * 显示加载对话框，设置了取消监听器此对话框可以中断，不设置则无法中断
     * 
     * @Title: showProgressDialogOfBase
     * @Description:
     * @param @param activity
     * @param @param listener
     * @return void
     * @throws
     */
    public static void showProgressDialog(Context context, String msg, OnCancelProgressDiaListener listener) {
        if (progressDialog == null || context != myContext) {
            myContext = context;

            closeProgressDialog();
            progressDialog = new CustomProgressDialog(context);
        }

        progressDialog.setInfo(msg);
        progressDialog.setOnCancelProgressDiaListener(listener);
        if (progressDialog.isShowing()) {
            return;
        }

        progressDialog.show();
    }

    /**
     * 显示加载对话框，设置了取消监听器此对话框可以中断，不设置则无法中断
     * 
     * @Title: showProgressDialogOfBase
     * @Description:
     * @param @param activity
     * @param @param listener
     * @return void
     * @throws
     */
    public static void showProgressDialogNoShadow(Context context, OnCancelProgressDiaListener listener) {
        if (progressDialog == null || context != myContext) {
            myContext = context;

            closeProgressDialog();
            progressDialog = new CustomProgressDialog(context, false);
        }

        progressDialog.setOnCancelProgressDiaListener(listener);
        if (progressDialog.isShowing()) {
            return;
        }

        progressDialog.show();
    }

    /**
     * 关闭加载对话框
     * 
     * @Title: closeProgressDialogOfBase
     * @Description:
     * @param
     * @return void
     * @throws
     */
    public static void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
