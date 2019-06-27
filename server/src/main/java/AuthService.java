public interface AuthService {
    String getName(String login, String password);
    void disconnect();
}
