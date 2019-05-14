package cn.emoney.acg.page.my;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.BitmapUtils;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.http.AsyncHttpResponseHandler;
import cn.emoney.sky.libs.network.HttpClient;
import cn.emoney.sky.libs.page.PageIntent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 裁剪图片的页面
 * 
 * @ClassName: CropImageActivity
 * @Description:
 * @author xiechengfa2000@163.com
 * @date 2015-5-8 下午3:39:22
 */
public class ClipImagePage extends PageImpl {
    public static final String RESULT_DATA = "crop_image";
    // xietest
    private static final String KEY = "path";

    private ClipImageLayout mClipImageLayout = null;

    public static void startPage(Fragment fragment, String path, int code) {
        PageImpl pageImpl = (PageImpl) fragment;
        PageIntent intent = new PageIntent(pageImpl, ClipImagePage.class);
        Bundle bundle = new Bundle();
        bundle.putString(KEY, path);
        intent.setArguments(bundle);
        pageImpl.startPageForResult(intent, code);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.crop_image_layout);
        Bundle budnBundle = getArguments();
        String path = null;
        if (budnBundle != null && budnBundle.containsKey(KEY)) {
            path = budnBundle.getString(KEY);
        }

        mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);

        int degreee = BitmapUtils.readBitmapDegree(path);
        Bitmap bitmap = BitmapUtils.createBitmapOfSampleSize(path, DataModule.SCREEN_WIDTH, DataModule.SCREEN_HEIGHT);
        if (bitmap != null) {
            if (degreee == 0) {
                mClipImageLayout.setImageBitmap(bitmap);
            } else {
                mClipImageLayout.setImageBitmap(BitmapUtils.rotaingBitmap(degreee, bitmap));
            }
        }

        bindPageTitleBar(R.id.clipImageTitleBar);
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        View rightView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView textView = (TextView) rightView.findViewById(R.id.tv_titlebar_text);
        textView.setText("提交");
        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            InputMethodUtil.closeSoftKeyBoard(this);
            finish();
        } else if (itemId == 2) {
            new ClipBitmapTask().execute();
        }
    }

    private class ClipBitmapTask extends AsyncTask<Object, Void, Message> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Message doInBackground(Object... params) {
            // TODO Auto-generated method stub
            Bitmap bitmap = mClipImageLayout.clip();

            // 缩成512X512
            bitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true);
            if (bitmap != null) {
                BitmapUtils.deleteAndSaveBitmap(AlbumAndCaptureDialog.TEMP_PATH, bitmap);
            }

            Message msg = Message.obtain();
            msg.obj = AlbumAndCaptureDialog.TEMP_PATH;
            return msg;
        }

        @Override
        protected void onPostExecute(Message result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            uploadImageFile();
        }
    }

    // 上传图片
    private void uploadImageFile() {
        String token = getUserInfo().getToken();
        final String url = DataModule.IMAGE_VOICE_URL + DataModule.FORMAT_PNG + "&token=" + token;
        HttpClient httpClient = new HttpClient(getContext());
        httpClient.uploadFileRequst(url, AlbumAndCaptureDialog.TEMP_PATH, new AsyncHttpResponseHandler() {
            public void onSuccess(String response) {
                LogUtil.easylog("onSuccess:" + response);
                closeProgressDialog();

                try {
                    JSONObject jsObj = JSON.parseObject(response);
                    int retCode = jsObj.getIntValue("retcode");
                    if (retCode == 1) {
                        // 成功
                        Bundle bundle = new Bundle();
                        bundle.putString(RESULT_DATA, jsObj.getString("retmsg"));
                        setResult(RESULT_CODE, bundle);
                        finish();
                    } else {
                        showTip("上传头像失败");
                    }
                } catch (Exception e) {
                    showTip("上传头像失败");
                }
            }

            @Override
            public void onStart() {
                LogUtil.easylog("onStart");
            }

            @Override
            public void onFinish() {
                closeProgressDialog();
                LogUtil.easylog("onFinish");
            }

            @Override
            public void onFailure(Throwable error, String content) {
                closeProgressDialog();
                showTip("上传头像失败");
                LogUtil.easylog("onFailure:" + error.getMessage() + ",content:" + content);
            }
        });
    }

    private void showProgressDialog() {
        DialogUtils.showProgressDialog(getActivity(), null);
    }

    private void closeProgressDialog() {
        DialogUtils.closeProgressDialog();
    }
}
