public interface IMyPlayerListener {

    void onStart(IMyPlayer player);

    void onPause(IMyPlayer player);

    void onResume(IMyPlayer player);

    void onComplete(IMyPlayer player);

}
