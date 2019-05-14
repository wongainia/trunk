package cn.emoney.acg.media;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import cn.emoney.acg.util.LogUtil;

/**
 * @ClassName: AppMediaPlayer
 * @Description:播放器
 * @author xiechengfa
 * @date 2015年12月8日 下午2:09:15
 *
 */
public class AppMediaPlayer implements OnCompletionListener, OnPreparedListener, OnErrorListener {
    private boolean isReleased = false;
    private boolean isPrepared = false;

    private String path = null;
    private MediaPlayer mediaPlayer = null;
    private MyMediaPlayerListener listener = null;

    public AppMediaPlayer(MyMediaPlayerListener listener) {
        this.listener = listener;
        isReleased = false;
        isPrepared = false;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
        }

        LogUtil.easylog("*********************KPlayer KuroMediaPlayer2");
    }

    /**
     * 获取播放PATH
     * 
     * @return
     */
    public String getDataSourcePath() {
        return path;
    }

    public void prepare() throws IllegalStateException, IOException {
        if (mediaPlayer != null && isPrepared == false) {
            mediaPlayer.prepareAsync();
        }
        LogUtil.easylog("*********************KPlayer prepare");
    }

    public void start() {
        if (mediaPlayer != null) {
            if (isPrepared) {
                mediaPlayer.start();
                LogUtil.easylog("*********************start");
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            LogUtil.easylog("*****************pause");
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            LogUtil.easylog("*******************stop");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            mediaPlayer.stop();
            isPrepared = false;
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer == null || isReleased == true) {
            return 0;
        }

        int currPos = 0;
        if (isPrepared) {
            currPos = mediaPlayer.getCurrentPosition();
        }
        return currPos;
    }

    public int getDuration() {
        int duration = 0;
        if (mediaPlayer != null && !isReleased && isPrepared) {
            duration = mediaPlayer.getDuration();
        } else {
            duration = 0;
        }
        return duration;
    }

    public boolean isPlaying() {
        if (mediaPlayer == null || isReleased == true) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    public void seekTo(int msec) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(msec);
        }
    }

    public void reset() {
        if (mediaPlayer != null) {
            LogUtil.easylog("*************reset");
            isPrepared = false;
            mediaPlayer.reset();
        }
    }

    public void setDataSourceAndPrepare(String path) {
        try {
            if (mediaPlayer != null) {
                LogUtil.easylog("******************setDateSource and prepare");
                this.path = path;
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();
            }
        } catch (Exception e) {
            // TODO: handle exception
            LogUtil.easylog("*************************setDataSource error:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Releases resources associated with this MediaPlayer object. It is considered good practice to
     * call this method when you're done using the MediaPlayer.
     */
    public void release() {
        if (mediaPlayer != null) {
            isReleased = true;
            isPrepared = false;
            mediaPlayer.release();
            mediaPlayer = null;
            LogUtil.easylog("**************************release");
        }
    }

    public void onPrepared(MediaPlayer mp) {
        LogUtil.easylog("***************************MediaPlayer onPrepared");
        isPrepared = true;
        if (listener != null) {
            listener.onPlayerStart();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogUtil.easylog("*******************MediaPlayer onError what:" + what + ",extra:" + extra);
        if (listener != null) {
            listener.onPlayerError();
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub
        LogUtil.easylog("************************MediaPlayer onCompletion");
        if (listener != null) {
            listener.onPlayerCompletion();
        }
    }


    /**
     * 播放器的监听器
     * 
     * @author xiechengfa
     * 
     */
    public interface MyMediaPlayerListener {
        /**
         * 准备完成，可以播放
         * 
         */
        public void onPlayerStart();

        /**
         * 播放器暂停
         */
        public void onPlayerPause();

        /**
         * 播放结束
         * 
         */
        public void onPlayerCompletion();

        /**
         * 加载播放器出错
         */
        public void onPlayerError();
    }
}
