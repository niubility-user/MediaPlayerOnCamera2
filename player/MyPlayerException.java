public class MyPlayerException extends Exception {
    public MyPlayerException(String detailMessage) {
        super(detailMessage);
    }

    public MyPlayerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MyPlayerException(Throwable throwable) {
        super(throwable);
    }
}
