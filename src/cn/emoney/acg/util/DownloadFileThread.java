package cn.emoney.acg.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;
import cn.emoney.acg.data.DataModule;

public class DownloadFileThread extends Thread {
    String m_downloadPath = "";
    private String m_url = "";
    private String m_fileName = "";
    private String m_dir = "";

    private boolean m_bCancelUpdate = false;
    DownloadFileCallBack callBack = null;

    public DownloadFileThread(String strUrl, String dir, String fileName, DownloadFileCallBack callBack) {
        this.m_dir = dir;
        this.m_url = strUrl;
        this.m_fileName = fileName;
        this.callBack = callBack;
    }

    public DownloadFileThread(String strUrl, String fileName, DownloadFileCallBack callBack) {
        this(strUrl, "", fileName, callBack);
    }

    @Override
    public void run() {
        try {
            m_bCancelUpdate = false;
            if (m_url == null || m_url.equals("") || m_fileName == null || m_fileName.equals("")) {
                if (callBack != null) {
                    callBack.onFail();
                    return;
                }
            }

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String t_path = Environment.getExternalStorageDirectory() + File.separator;
                m_downloadPath = t_path + DataModule.G_LOC_PATH + "download";
                if (m_dir != null && !m_dir.equals("")) {
                    m_downloadPath += (File.separator + m_dir + File.separator);
                }
                URL t_url = new URL(this.m_url);

                HttpURLConnection conn = (HttpURLConnection) t_url.openConnection();

                if (callBack != null) {
                    callBack.onStart();
                }

                conn.connect();

                int t_length = conn.getContentLength();

                InputStream is = conn.getInputStream();

                File fileDir = new File(m_downloadPath);

                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }

                t_path = m_downloadPath + File.separator + m_fileName;
                File file = new File(t_path);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }

                File downFile = new File(m_downloadPath, m_fileName + "_temp");
                FileOutputStream fos = new FileOutputStream(downFile);

                int t_count = 0;

                byte buf[] = new byte[1024];

                do {
                    int t_numRead = is.read(buf);
                    if (t_numRead > 0) {
                        t_count += t_numRead;
                        fos.write(buf, 0, t_numRead);
                    }
                    int t_progress = (int) (((float) t_count / t_length) * 100);

                    if (callBack != null) {
                        callBack.onProcess(t_progress);
                    }

                    if (t_numRead < 0 || t_count >= t_length) {
                        break;
                    }

                } while (m_bCancelUpdate == false);

                fos.close();
                is.close();
                conn.disconnect();

                File apkFile = new File(m_downloadPath, m_fileName);
                if (downFile.renameTo(apkFile)) {
                    // 回调通知
                    if (m_bCancelUpdate == false) {
                        if (callBack != null) {
                            callBack.onSuccess(m_downloadPath + File.separator + m_fileName);
                        }
                    }
                }
            } else {
                if (callBack != null) {
                    callBack.onFail();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            callBack.onFail();
        }

    }

    public void cancel(boolean b) {
        m_bCancelUpdate = b;
    }

    // 下载状态进度回调接口
    public static interface DownloadFileCallBack {
        void onProcess(int process);

        void onSuccess(String path);

        void onFail();

        void onStart();
    }

}
