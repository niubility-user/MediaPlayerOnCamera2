
public class MediaPlayerHelper implements IMyPlayer, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnErrorListener, SurfaceHolder.Callback {

    /**
     * MediaPlayer实体
     */
    private MediaPlayer mediaPlayer;
    /**
     * 播放资源完整路径
     */
    private String videoSource;

    /**
     * 显示-接口
     */
    private IMyDisplay display;

    /**
     * 视频宽高是否已获取，且不为0
     */
    private boolean isVideoSizeMeasured = false;
    /**
     * 视频资源是否准备完成
     */
    private boolean isMediaPrepared = false;
    /**
     * Surface是否被创建
     */
    private boolean isSurfaceCreated = false;
    /**
     * 使用者是否打算播放
     */
    private boolean isUserWantToPlay = false;
    /**
     * 是否在Resume状态
     */
    private boolean isResumed = false;

    /**
     * 是否播放完毕
     */
    private boolean isCompleted = false;

    /**
     * 是否被裁减
     */
    private boolean mIsCrop = false;

    /**
     * 监听器
     */
    private IMyPlayerListener myPlayerListener;


    private RecordVideoActivity.SurfaceSizeChangeListener surfaceSizeChangeListener;
    /**
     * 当前视频宽度
     */
    private int currentVideoWidth;
    /**
     * 当前视频高度
     */
    private int currentVideoHeight;

    /**
     * 创建MediaPlayer
     */
    private void createPlayer() {
        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
        }
    }

    /**
     * 开始播放
     */
    private void playStart() {
        if (isVideoSizeMeasured && isMediaPrepared && isSurfaceCreated && isUserWantToPlay && isResumed) {
            mediaPlayer.setDisplay(display.getHolder());
            mediaPlayer.start();
            isCompleted = false;
            log("视频开始播放");
            display.onStart(this);
            if (myPlayerListener != null) {
                myPlayerListener.onStart(this);
            }
        }
    }

    /**
     * 暂停播放
     */
    private void playPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            display.onPause(this);
            if (myPlayerListener != null) {
                myPlayerListener.onPause(this);
            }
        }
    }

    /**
     * 停止播放,并释放资源
     */
    private void playStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            display.onComplete(this);
            if (myPlayerListener != null) {
                myPlayerListener.onComplete(this);
            }
        }
    }


    /**
     * 播放前检查资源状态
     *
     * @return 播放资源是否准备好
     */
    private boolean checkSource() {
        return videoSource != null && videoSource.length() != 0;
    }

    /**
     * 设置播放监听
     *
     * @param myPlayerListener 监听器
     */
    public void setMyPlayerListener(IMyPlayerListener myPlayerListener) {
        this.myPlayerListener = myPlayerListener;
    }

    public void setSurfaceSizeChangeListener(RecordVideoActivity.SurfaceSizeChangeListener surfaceSizeChangeListener) {
        this.surfaceSizeChangeListener = surfaceSizeChangeListener;
    }

    /**
     * 设置是否裁剪视频，若裁剪，则视频按照DisplayView的父布局大小显示。
     * 若不裁剪，视频居中于DisplayView的父布局显示
     *
     * @param isCrop 是否裁剪视频
     */
    public void setCrop(boolean isCrop) {
        this.mIsCrop = isCrop;
        if (display != null && currentVideoWidth > 0 && currentVideoHeight > 0) {
            tryResetSurfaceSize(display.getDisplayView(), currentVideoWidth, currentVideoHeight);
        }
    }

    /**
     * 是否被裁剪
     */
    public boolean isCrop() {
        return mIsCrop;
    }

    /**
     * 视频状态
     *
     * @return 视频是否正在播放
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * 判断是否播放完毕
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * 根据设置和视频尺寸，调整视频播放区域的大小
     *
     * @param view        播放控件
     * @param videoWidth  视频宽度
     * @param videoHeight 视频高度
     */
    private void tryResetSurfaceSize(final View view, int videoWidth, int videoHeight) {
        ViewGroup parent = (ViewGroup) view.getParent();
        int width = parent.getWidth();
        int height = parent.getHeight();
        if (width > 0 && height > 0) {
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            if (mIsCrop) {
                float scaleVideo = videoWidth / (float) videoHeight;
                float scaleSurface = width / (float) height;
                if (scaleVideo < scaleSurface) {
                    params.width = width;
                    params.height = (int) (width / scaleVideo);
                    params.setMargins(0, (height - params.height) / 2, 0, (height - params.height) / 2);
                } else {
                    params.height = height;
                    params.width = (int) (height * scaleVideo);
                    params.setMargins((width - params.width) / 2, 0, (width - params.width) / 2, 0);
                }
            } else {
                if (videoWidth > width || videoHeight > height) {
                    float scaleVideo = videoWidth / (float) videoHeight;
                    float scaleSurface = width / (float) height;
                    if (scaleVideo > scaleSurface) {
                        params.width = width;
                        params.height = (int) (width / scaleVideo);
                        params.setMargins(0, (height - params.height) / 2, 0, (height - params.height) / 2);
                    } else {
                        params.height = height;
                        params.width = (int) (height * scaleVideo);
                        params.setMargins((width - params.width) / 2, 0, (width - params.width) / 2, 0);
                    }
                }
            }
            view.setLayoutParams(params);
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    surfaceSizeChangeListener.sizeChange();
                }
            });
        }
    }

    @Override
    public void setSource(String url) throws MyPlayerException {
        this.videoSource = url;
        createPlayer();
        isMediaPrepared = false;
        isVideoSizeMeasured = false;
        currentVideoWidth = 0;
        currentVideoHeight = 0;
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            log("阻塞式准备视频");
        } catch (IOException e) {
            throw new MyPlayerException("set source error", e);
        }
    }

    @Override
    public void setDisplay(IMyDisplay display) {
        if (this.display != null && this.display.getHolder() != null) {
            this.display.getHolder().removeCallback(this);
        }
        this.display = display;
        this.display.getHolder().addCallback(this);
    }


    @Override
    public void play() throws MyPlayerException {
        if (!checkSource()) {
            throw new MyPlayerException("Please setSource");
        }
        createPlayer();
        isUserWantToPlay = true;
        playStart();
    }

    @Override
    public void pause() {
        isUserWantToPlay = false;
        playPause();
    }

    @Override
    public void stop() {
        isUserWantToPlay = false;
        playStop();
    }

    @Override
    public int getSourceDuration() throws MyPlayerException {
        if (!checkSource()) {
            throw new MyPlayerException("Please setSource");
        }
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() throws MyPlayerException {
        if (!checkSource()) {
            throw new MyPlayerException("Please setSource");
        }
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(long time) throws MyPlayerException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!checkSource()) {
                throw new MyPlayerException("Please setSource");
            }
            // 播放中,则先暂停
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            mediaPlayer.seekTo(time, MediaPlayer.SEEK_PREVIOUS_SYNC);
        } else {
            throw new MyPlayerException("current Platform < 26 , not support seekTo");
        }
    }

    @Override
    public void onPause() {
        isResumed = false;
        playPause();
    }

    @Override
    public void onResume() {
        isResumed = true;
        playStart();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        display.onComplete(this);
        if (myPlayerListener != null) {
            myPlayerListener.onComplete(this);
        }
        isCompleted = true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        log("视频准备完成");
        isMediaPrepared = true;
        playStart();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        log("视频大小被改变-> [" + width + "," + height + "]");
        if (width > 0 && height > 0) {
            this.currentVideoWidth = width;
            this.currentVideoHeight = height;
            tryResetSurfaceSize(display.getDisplayView(), width, height);
            isVideoSizeMeasured = true;
            playStart();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        log("定位完成");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        log("OnError - Error code: " + what + " Extra code: " + extra);
        switch (what) {
            case -1004:
                log("MEDIA_ERROR_IO");
                break;
            case -1007:
                log("MEDIA_ERROR_MALFORMED");
                break;
            case 200:
                log("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                break;
            case 100:
                log("MEDIA_ERROR_SERVER_DIED");
                break;
            case -110:
                log("MEDIA_ERROR_TIMED_OUT");
                break;
            case 1:
                log("MEDIA_ERROR_UNKNOWN");
                break;
            case -1010:
                log("MEDIA_ERROR_UNSUPPORTED");
                break;
        }
        switch (extra) {
            case 800:
                log("MEDIA_INFO_BAD_INTERLEAVING");
                break;
            case 702:
                log("MEDIA_INFO_BUFFERING_END");
                break;
            case 701:
                log("MEDIA_INFO_METADATA_UPDATE");
                break;
            case 802:
                log("MEDIA_INFO_METADATA_UPDATE");
                break;
            case 801:
                log("MEDIA_INFO_NOT_SEEKABLE");
                break;
            case 1:
                log("MEDIA_INFO_UNKNOWN");
                break;
            case 3:
                log("MEDIA_INFO_VIDEO_RENDERING_START");
                break;
            case 700:
                log("MEDIA_INFO_VIDEO_TRACK_LAGGING");
                break;
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (display != null && holder == display.getHolder()) {
            isSurfaceCreated = true;
            //此举保证以下操作下，不会黑屏。（或许还是会有手机黑屏）
            //暂停，然后切入后台，再切到前台，保持暂停状态
            if (mediaPlayer != null) {
                mediaPlayer.setDisplay(holder);
                //不加此句360f4不会黑屏、小米note1会黑屏，其他机型未测
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
            }
            log("surface被创建");
            playStart();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        log("surface大小改变:" + "[" + width + "," + height + "]");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (display != null && holder == display.getHolder()) {
            log("surface被销毁");
            isSurfaceCreated = false;
        }
    }

    private void log(String content) {
        Log.e("MediaPlayerHelper", content);
    }
}
