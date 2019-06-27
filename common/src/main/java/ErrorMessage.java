public class ErrorMessage extends AbstractMessage {
    private Exception exception;
    private Const.AlertText alertText;
    private String contentText;

    public ErrorMessage(Exception exception, Const.AlertText alertText, String contentText) {
        this.exception = exception;
        this.alertText = alertText;
        this.contentText = contentText;
    }

    public Exception getException() {
        return exception;
    }

    public Const.AlertText getAlertText() {
        return alertText;
    }

    public String getContentText() {
        return contentText;
    }
}
