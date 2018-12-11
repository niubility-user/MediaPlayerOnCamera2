public interface IMyDisplay extends IMyPlayerListener {

    /**
     * @return 获取播放载体的View, SurfaceView或TextureView或SurfaceTexture
     */
    View getDisplayView();

    /**
     * @return 获取SurfaceHolder
     */
    SurfaceHolder getHolder();
}
