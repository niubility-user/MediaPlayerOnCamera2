public interface IMyPlayer {
    /**
     * 设置资源
     *
     * @param url 资源路径
     * @throws MyPlayerException
     */
    void setSource(String url) throws MyPlayerException;

    /**
     * 设置显示视频的载体
     *
     * @param display 视频播放的载体及相关界面
     */
    void setDisplay(IMyDisplay display);

    /**
     * 播放视频
     *
     * @throws MyPlayerException
     */
    void play() throws MyPlayerException;

    /**
     * 暂停视频
     */
    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 获取资源的总时长
     *
     * @return 时长毫秒数
     */
    int getSourceDuration() throws MyPlayerException;

    /**
     * 获取当前播放的位置
     *
     * @return 毫秒数
     */
    int getCurrentPosition() throws MyPlayerException;

    /**
     * <p>移动播放位置</p>
     * <p>若当前是播放状态,则<font color='red'>暂停</font>播放</p>
     *
     * @param time 移动到的位置,毫秒数表示
     */
    void seekTo(long time) throws MyPlayerException;


    /**
     * 用于Activity的onPause状态
     */
    void onPause();

    /**
     * 用于Activity的onResume状态
     */
    void onResume();

    /**
     * 用于Activity的onDestroy状态
     */
    void onDestroy();
}
