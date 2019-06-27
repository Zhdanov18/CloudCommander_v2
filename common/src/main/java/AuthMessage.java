public class AuthMessage extends AbstractMessage{
    private String name;

    public String getName() { return name; }

    public AuthMessage(String name) { this.name = name; }
}
