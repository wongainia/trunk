package cn.emoney.acg.media;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.os.Environment;
import android.text.TextUtils;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.helper.FixedLengthList;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.DownloadFileThread;
import cn.emoney.acg.util.DownloadFileThread.DownloadFileCallBack;
import cn.emoney.acg.util.FileUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.MD5Util;

public class AudioRecordCacheManager {
    private final int MAX_DIR_COUNT = 5;// 文件夹数量
    private final int MAX_FILE_COUNT_PER_DIR = 20;// 每个文件夹的文件数量

    private static AudioRecordCacheManager mInstance = null;

    private AudioCacheBackListener mCurListener = null;

    private long recordId = 0;
    private String mCacheRootDir = "";
    private FixedLengthList<String> mLstCacheDir = new FixedLengthList<String>(MAX_DIR_COUNT);

    /**
     * key: 录音文件名; value:目录名(timestamp)
     */
    private Map<String, String> mMapCache = new HashMap<String, String>();

    private AudioRecordCacheManager() {
        initCacheIndex();
    }

    public static AudioRecordCacheManager getInstance() {
        if (mInstance == null) {
            mInstance = new AudioRecordCacheManager();
        }

        return mInstance;
    }

    public void setListener(AudioCacheBackListener listener) {
        mCurListener = listener;
    }

    /**
     * 
     * @param url 录音url地址
     * 
     */
    public void getLocPathByUrl(String url, long recordId) {
        this.recordId = recordId;

        if (TextUtils.isEmpty(url)) {
            if (mCurListener != null) {
                mCurListener.onRecordCacheFail(recordId);
            }
        }

        if (mMapCache != null) {
            String fileName = MD5Util.md5_16(url);
            if (mMapCache.containsKey(fileName)) {
                String filePath = mCacheRootDir + mMapCache.get(fileName) + File.separator + fileName;
                File cacheFile = new File(filePath);
                if (cacheFile.exists()) {
                    cacheFile = null;
                    if (mCurListener != null) {
                        mCurListener.onRecordCacheSucc(filePath, recordId);
                    }
                    return;
                }
            }

            doDownloadRecordFile(url, fileName);
        }
    }

    /**
     * 初始化cache索引
     */
    private void initCacheIndex() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String t_path = Environment.getExternalStorageDirectory() + File.separator;
            mCacheRootDir = t_path + DataModule.G_LOC_PATH + "recordcache" + File.separator;

            File fRootDir = new File(mCacheRootDir);

            if (!fRootDir.exists()) {
                fRootDir.mkdir();
                return;
            }
            if (fRootDir.isDirectory()) {
                File[] lstCacheDir = fRootDir.listFiles();
                if (lstCacheDir != null && lstCacheDir.length > 0) {

                    mMapCache.clear();
                    mLstCacheDir.clear();
                    for (int i = 0; i < lstCacheDir.length; i++) {
                        // LogUtil.easylog("recordcache CacheDirName->" + i + ":" +
                        // lstCacheDir[i].getName());
                        // LogUtil.easylog("recordcache lstCacheDirPath->" + i + ":" +
                        // lstCacheDir[i].getPath());

                        String dirName = lstCacheDir[i].getName();
                        String dirPath = lstCacheDir[i].getPath();
                        mLstCacheDir.addLastSafe(dirName);
                        File fCacheDir = new File(dirPath);
                        if (fCacheDir.isDirectory()) {
                            File[] lstCacheFile = fCacheDir.listFiles();
                            if (lstCacheFile != null) {
                                for (int j = 0; j < lstCacheFile.length; j++) {
                                    mMapCache.put(lstCacheFile[j].getName(), dirName);
                                }
                            }
                        }
                    }

                    Collections.sort(mLstCacheDir, new CacheDirComparator());
                    LogUtil.easylog("mLstCacheDir:" + mLstCacheDir);
                }
            }
        }
    }


    private class CacheDirComparator implements Comparator<String> {
        @Override
        public int compare(String lhs, String rhs) {
            if (TextUtils.isEmpty(lhs) || TextUtils.isEmpty(rhs)) {
                return 0;
            } else if (lhs.equals(rhs)) {
                return 0;
            } else if (DataUtils.convertToLong(lhs) > DataUtils.convertToLong(rhs)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private void doCacheRecordFile(String path) {
        File fTempCache = null;
        if (TextUtils.isEmpty(path)) {
            if (mCurListener != null) {
                mCurListener.onRecordCacheFail(recordId);
            }
            return;
        }
        fTempCache = new File(path);
        if (!fTempCache.exists()) {
            if (mCurListener != null) {
                mCurListener.onRecordCacheFail(recordId);
            }
            return;
        }

        if (mLstCacheDir.size() == 0) {
            long timestamp = DateUtils.getTimeStamp() / 1000;
            File fNewCacheDir = new File(mCacheRootDir + timestamp);
            mLstCacheDir.addLastSafe(timestamp + "");
            fNewCacheDir.mkdir();
            fNewCacheDir = null;
        }

        String cacheDirName = mLstCacheDir.get(mLstCacheDir.size() - 1);
        String cahceDir = mCacheRootDir + cacheDirName + File.separator;
        File fCacheDir = new File(cahceDir);
        int fileCount = fCacheDir.listFiles().length;
        if (fileCount < MAX_FILE_COUNT_PER_DIR) {
            // 文件夹里的文件操作
            String dstFileName = fTempCache.getName();
            String dstFilePath = cahceDir + dstFileName;
            boolean bRet = fTempCache.renameTo(new File(dstFilePath));
            if (bRet == true) {
                if (mCurListener != null) {
                    mMapCache.put(dstFileName, cacheDirName);
                    mCurListener.onRecordCacheSucc(dstFilePath, recordId);
                }
            } else {
                if (mCurListener != null) {
                    mCurListener.onRecordCacheFail(recordId);
                }
            }
        } else {
            // 添加文件夹
            if (mLstCacheDir.size() >= MAX_DIR_COUNT) {
                // 删除第一个文件夹
                String oldestDir = mCacheRootDir + mLstCacheDir.get(0) + File.separator;
                File fOldestDir = new File(oldestDir);
                File[] lstOldestFile = fOldestDir.listFiles();
                for (int i = 0; i < lstOldestFile.length; i++) {
                    String fileName = lstOldestFile[i].getName();
                    if (mMapCache.containsKey(fileName)) {
                        mMapCache.remove(fileName);
                    }
                    lstOldestFile[i].delete();
                }

                FileUtils.deleteDir(fOldestDir);
                mLstCacheDir.remove(0);
            }

            // 添加新的文件夹
            long timestamp = DateUtils.getTimeStamp() / 1000;
            File fNewCacheDir = new File(mCacheRootDir + timestamp);
            mLstCacheDir.addLastSafe(timestamp + "");
            fNewCacheDir.mkdir();

            String dstFileName = fTempCache.getName();
            File fDestFile = new File(fNewCacheDir, dstFileName);
            boolean bRet = fTempCache.renameTo(fDestFile);
            if (bRet == true) {
                mMapCache.put(dstFileName, timestamp + "");
                if (mCurListener != null) {
                    mCurListener.onRecordCacheSucc(fDestFile.getPath(), recordId);
                }
            } else {
                if (mCurListener != null) {
                    mCurListener.onRecordCacheFail(recordId);
                }
            }
        }
    }

    // 请先check再update
    private void doDownloadRecordFile(String downUrl, String fileName) {
        new DownloadFileThread(downUrl, fileName, new DownloadFileCallBack() {
            private long mLastTime;

            @Override
            public void onStart() {
                mLastTime = DateUtils.getTimeStamp();
                LogUtil.easylog("doTestDownload->onStart:" + mLastTime);
            }

            @Override
            public void onSuccess(String path) {
                long tTime = DateUtils.getTimeStamp();
                LogUtil.easylog("doTestDownload->onSuccess:" + tTime);
                String loginfo = DataUtils.mZDFFormat.format((tTime - mLastTime) / 1000f) + "秒";
                LogUtil.easylog("doTestDownload->耗时:" + loginfo);

                doCacheRecordFile(path);
            }

            @Override
            public void onProcess(int process) {}

            @Override
            public void onFail() {
                LogUtil.easylog("doTestDownload:Err");
                if (mCurListener != null) {
                    mCurListener.onRecordCacheFail(recordId);
                }
            }
        }).start();
    }

    public interface AudioCacheBackListener {
        /**
         * 缓存成功
         * 
         * @param path
         */
        public void onRecordCacheSucc(String path, long recordId);

        /**
         * 缓存失败
         */
        public void onRecordCacheFail(long recordId);
    }
}
