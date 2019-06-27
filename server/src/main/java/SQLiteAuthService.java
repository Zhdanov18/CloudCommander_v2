import java.sql.*;

public class SQLiteAuthService implements AuthService {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement ps;

    public SQLiteAuthService(){
        try {
            connect();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    @Override
    public void disconnect() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName(String login, String password) {
        try {
            ps = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean changeNickname(String oldNick, String newNick){
        try {
            ps = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");
            ps.setString(1, newNick);
            ps.setString(2, oldNick);
            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
