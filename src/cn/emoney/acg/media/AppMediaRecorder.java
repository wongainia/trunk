package cn.emoney.acg.media;

import java.io.File;

import android.media.MediaRecorder;
import cn.emoney.acg.util.LogUtil;

/**
 * @ClassName: AppMediaRecorder
 * @Description:录音
 * @author xiechengfa
 * @date 2015年12月23日 上午10:01:07
 *
 */
public class AppMediaRecorder {
    private boolean isRecord = false;
    private String path = null;
    private MediaRecorder recorder = null;

    public AppMediaRecorder() {}

    public void start(String path) {
        this.path = path;
        startRecorder();
        isRecord = true;
        LogUtil.easylog("*********************start");
    }

    public void stop() {
        stopRecorder();
        isRecord = false;
        LogUtil.easylog("*********************stop");
    }

    public void pause() {
        stopRecorder();
        isRecord = false;
        LogUtil.easylog("*********************pause");
    }

    public void restart() {
        startRecorder();
        isRecord = true;
        LogUtil.easylog("*********************restart");
    }

    public boolean isPause() {
        return !isRecord;
    }

    private void startRecorder() {
        try {
            // 删除存在的文件
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }

            if (recorder == null) {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile(path);
                recorder.prepare();
                recorder.start();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void stopRecorder() {
        if (recorder != null) {
            if (isRecord) {
                recorder.stop();
            }
            recorder.release();
            recorder = null;
        }
    }
}
