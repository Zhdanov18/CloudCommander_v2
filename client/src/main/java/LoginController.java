import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {
    private String textLogin;
    private String textPassword;

    public String getLogin() { return textLogin; }

    public String getPassword() { return textPassword; }

    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox loginWindow;

    public int id;

    public void auth(ActionEvent actionEvent) {
        textLogin = login.getText().trim();
        textPassword = password.getText().trim();
        loginWindow.getScene().getWindow().hide();
    }
}
