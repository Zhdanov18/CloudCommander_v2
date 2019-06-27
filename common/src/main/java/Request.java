public class Request extends AbstractMessage {
    private Const.Action action;
    private String[] arg;

    public Request(Const.Action action, String ...arg) {
        this.action = action;
        this.arg = arg;
    }

    public Const.Action getAction() { return action; }
    public String getArg(int index) { return arg[index]; }
}
