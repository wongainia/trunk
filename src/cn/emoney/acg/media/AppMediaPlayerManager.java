package cn.emoney.acg.media;

import cn.emoney.acg.media.AppMediaPlayer.MyMediaPlayerListener;
import cn.emoney.acg.util.LogUtil;


/**
 * 播放器的管理类
 * 
 * @ClassName: AppMediaPlayerManager
 * @Description:
 * @author xiechengfa
 * @date 2015年12月8日 上午10:59:49
 *
 */
public class AppMediaPlayerManager implements MyMediaPlayerListener {
    public static final int PLAY_STATE_NONE = 0;// 播放状态-没有
    public static final int PLAY_STATE_PAUSE = 1;// 播放状态-暂停
    public static final int PLAY_STATE_PLAY = 2;// 播放状态-正在播放
    public static final int PLAY_STATE_OVER = 3;// 播放状态-播放结束
    public static final int PLAY_STATE_LOADING = 4;// 播放状态－正在加载


    private boolean isOnForGround = false;// 当前页面是否在前台
    private int playState = PLAY_STATE_NONE;// 播放状态
    private String currPath = null;
    private AppMediaPlayer mediaPlayer = null;
    private MyMediaPlayerListener listener = null;

    public AppMediaPlayerManager(MyMediaPlayerListener listener) {
        this.listener = listener;
        isOnForGround = true;
    }

    public void setPageState(boolean isOnForGround) {
        this.isOnForGround = isOnForGround;
    }

    public void onStartPlayer(String path) {
        if (!isOnForGround) {
            // 不在前台
            return;
        }

        if (path == null || path.trim().length() <= 0) {
            return;
        }

        if (path.equals(currPath)) {
            // 同一个资源
            if (playState == PLAY_STATE_LOADING) {
                // 正在加载
                return;
            } else if (playState == PLAY_STATE_PLAY) {
                // 正在播放，则暂停
                onPause();
                return;
            } else if (playState == PLAY_STATE_PAUSE) {
                // 暂停，则重新播放
                // onPlayerStart();
                // return;
            }
        }

        this.currPath = path;
        try {
            // 创建播放器
            if (mediaPlayer == null) {
                createMediaPlayer();
            } else {
                onReleasePlayer();
                createMediaPlayer();
            }

            mediaPlayer.setDataSourceAndPrepare(path);
            playState = PLAY_STATE_LOADING;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    public int getCurrentPosition() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        if (mediaPlayer == null) {
            return 0;
        }

        return mediaPlayer.getDuration();
    }

    // /**
    // * 当前的资源是否正在播放
    // *
    // * @param path
    // * @return
    // */
    // public boolean isCurrPathPlaying(String path) {
    // if (path != null && path.equals(currPath) && playState == PLAY_STATE_PLAY) {
    // return true;
    // } else {
    // return false;
    // }
    // }
    //
    // /**
    // * 当前的资源是否正在播放
    // *
    // * @param path
    // * @return
    // */
    // public boolean isCurrPathPause(String path) {
    // if (path != null && path.equals(currPath) && playState == PLAY_STATE_PAUSE) {
    // return true;
    // } else {
    // return false;
    // }
    // }

    public boolean isPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return true;
        }

        return false;
    }

    public void onPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        playState = PLAY_STATE_PAUSE;

        if (listener != null) {
            listener.onPlayerPause();
        }
    }

    public void onStopPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
        playState = PLAY_STATE_NONE;
    }

    public void onReleasePlayer() {
        if (mediaPlayer != null) {
            onStopPlayer();
            startReleasePlayerRunable(mediaPlayer);
        }
    }

    /**
     * 准备完成，可以播放
     * 
     */
    public void onPlayerStart() {
        // TODO Auto-generated method stub
        if (mediaPlayer != null && currPath != null && currPath.equals(mediaPlayer.getDataSourcePath())) {
            playState = PLAY_STATE_PLAY;
            mediaPlayer.start();
            if (listener != null) {
                listener.onPlayerStart();
            }
        }
    }

    /**
     * 播放器暂停
     */
    public void onPlayerPause() {
        // 不要实现
    }

    /**
     * 播放结束
     * 
     */
    public void onPlayerCompletion() {
        // TODO Auto-generated method stub
        if (mediaPlayer != null && currPath != null && currPath.equals(mediaPlayer.getDataSourcePath())) {
            // 停止播放器
            onStopPlayer();
        }

        if (listener != null) {
            listener.onPlayerCompletion();
        }
    }

    /**
     * 加载播放器出错
     */
    public void onPlayerError() {
        // TODO Auto-generated method stub
        playState = PLAY_STATE_NONE;
        if (listener != null) {
            listener.onPlayerError();
        }
    }


    // 创建播放器
    private void createMediaPlayer() {
        mediaPlayer = new AppMediaPlayer(this);
    }

    private void startReleasePlayerRunable(final AppMediaPlayer tempMediaPlayer) {
        new Thread(new Runnable() {
            AppMediaPlayer tempPlayer = tempMediaPlayer;

            @Override
            public void run() {
                // TODO Auto-generated method stub
                tempPlayer.release();
                tempPlayer = null;
                LogUtil.easylog("*********************startReleasePlayerRunable");
            }
        }).start();
    }
}
