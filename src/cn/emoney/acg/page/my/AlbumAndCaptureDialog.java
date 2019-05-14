package cn.emoney.acg.page.my;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.dialog.ListDialog.OnListDialogItemClickListener;
import cn.emoney.acg.dialog.ListDialogMenuInfo;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.FileUtils;

/**
 * 打开相册和相机对话框
 * 
 * @类说明:
 * @作者:xiechengfa
 * @创建时间:2014-9-5 下午4:26:05
 */
public class AlbumAndCaptureDialog implements OnListDialogItemClickListener {
    // 用来标识请求照相功能
    private static final int START_CAPTURE_CODE = 100;
    // 用来标识请求gallery
    private static final int START_ALBUM_CODE = 101;
    // 裁剪的标识
    private final int CROP_RESULT_CODE = 102;
    public static final String TEMP_PATH = FileUtils.getStoragePath() + DataModule.G_LOC_PATH + "head_temp";

    private Fragment fragment = null;
    private Activity activity = null;
    private LoadAlbumAndCameraCallBack callBack = null;

    public AlbumAndCaptureDialog(Fragment fragment, Activity activity, LoadAlbumAndCameraCallBack callBack) {
        this.fragment = fragment;
        this.activity = activity;
        this.callBack = callBack;
    }

    public void show() {
        DialogUtils.showListDialog(activity, getChooseItems(), this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
        // case CROP_RESULT_CODE:
        // // 裁剪图片的结果回调
        // if (callBack != null) {
        // callBack.imageCallBack(data.getStringExtra(ClipImagePage.RESULT_DATA));
        // }
        // break;
            case START_ALBUM_CODE:
                // 相册返回
                startCropImageActivity(getAlbumFilePath(data.getData()));
                break;
            case START_CAPTURE_CODE:
                // 照相机程序返回的,再次调用图片剪辑程序去修剪图片
                startCropImageActivity(TEMP_PATH);
                break;
        }
    }

    public void onPageResult(int requestCode, int resultCode, Bundle data) {
        if (resultCode != PageImpl.RESULT_CODE) {
            return;
        }

        if (requestCode == CROP_RESULT_CODE) {
            // 裁剪图片的结果回调
            if (callBack != null) {
                callBack.imageCallBack(data.getString(ClipImagePage.RESULT_DATA));
            }
        }
    }

    // 选择头像照片菜单
    private ArrayList<ListDialogMenuInfo> getChooseItems() {
        ArrayList<ListDialogMenuInfo> list = new ArrayList<ListDialogMenuInfo>();
        list.add(new ListDialogMenuInfo(0, R.drawable.img_camera, "拍照"));
        list.add(new ListDialogMenuInfo(1, R.drawable.img_album, "相册"));
        return list;
    }

    @Override
    public void onItemClicked(int position) {
        switch (position) {
            case 0:
                // 拍照
                if (FileUtils.isExistsStorage()) {
                    // sd卡存在
                    startCapture(activity, TEMP_PATH, START_CAPTURE_CODE);
                } else {
                    // sd卡不存在
                    Toast.makeText(activity, "Sdcard不存在", Toast.LENGTH_LONG).show();
                }
                break;
            case 1:
                // 手机相册
                startAlbum(activity, START_ALBUM_CODE);
                break;
        }
    }

    /**
     * 启动手机相册
     * 
     * @Title: startAlbum
     * @Description:
     * @param @param activity
     * @param @param code
     * @return void
     * @throws
     */
    private void startAlbum(Activity activity, int code) {
        // 抓下异常是防止有的机器不支持ACTION_PICK或ACTION_GET_CONTENT的动作
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            fragment.startActivityForResult(intent, code);
        } catch (Exception e1) {
            // TODO: handle exception
            e1.printStackTrace();
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("image/*");
                fragment.startActivityForResult(intent, code);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                Toast.makeText(activity, "打开相册失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 启动相机
     * 
     * @Title: startCapture
     * @Description:
     * @param @param activity
     * @return void
     * @throws
     */
    private void startCapture(Activity activity, String path, int code) {
        try {
            // 创建照片的存储目录
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path)));
            fragment.startActivityForResult(intent, code);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(activity, "打开相机失败", Toast.LENGTH_LONG).show();
        }
    }

    // 裁剪图片的Activity
    private void startCropImageActivity(String path) {
        ClipImagePage.startPage(fragment, path, CROP_RESULT_CODE);
        // ClipImageActivity.startPage(fragment.getActivity(), path,
        // CROP_RESULT_CODE);
    }

    public interface LoadAlbumAndCameraCallBack {
        /**
         * 调用相册和相机的回调
         * 
         * @param bitmap
         */
        public void imageCallBack(String tempPath);
    }

    /**
     * 通过uri获取文件路径
     * 
     * @param mUri
     * @return
     */
    private String getAlbumFilePath(Uri mUri) {
        try {
            if (mUri.getScheme().equals("file")) {
                return mUri.getPath();
            } else {
                return getFilePathByUri(mUri);
            }
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    // 获取文件路径通过url
    private String getFilePathByUri(Uri mUri) throws FileNotFoundException {
        String imgPath;
        ContentResolver mContentResolver = ACGApplication.getInstance().getContentResolver();
        Cursor cursor = mContentResolver.query(mUri, null, null, null, null);
        cursor.moveToFirst();
        imgPath = cursor.getString(1);
        return imgPath;
    }
}
