public class MySimpleDisplay implements IMyDisplay {
    private SurfaceView surfaceView;

    public MySimpleDisplay(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    @Override
    public View getDisplayView() {
        return surfaceView;
    }

    @Override
    public SurfaceHolder getHolder() {
        return surfaceView.getHolder();
    }

    @Override
    public void onStart(IMyPlayer player) {
        LogUtil.getInstance().d("MySimpleDisplay", "播放开始");
    }

    @Override
    public void onPause(IMyPlayer player) {
        LogUtil.getInstance().d("MySimpleDisplay", "播放暂停");
    }

    @Override
    public void onResume(IMyPlayer player) {
        LogUtil.getInstance().d("MySimpleDisplay", "播放恢复");
    }

    @Override
    public void onComplete(IMyPlayer player) {
        LogUtil.getInstance().d("MySimpleDisplay", "播放完毕");
    }
}
