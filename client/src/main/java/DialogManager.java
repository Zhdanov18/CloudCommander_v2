import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class DialogManager {

    public static boolean confirmMsg(Const.AlertText alertText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(alertText.getTitle());
        alert.setHeaderText(alertText.getHeaderText());
        alert.setContentText(contentText);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getText().equals("OK")) {
            return true;
        }
        return false;
    }

    public static void errorMsg(Const.AlertText alertText, String contentText, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(alertText.getTitle());
        alert.setHeaderText(alertText.getHeaderText());

        if (exception != null) {
            VBox dialogPaneContent = new VBox();
            Label label = new Label("Stack Trace:");

            String stackTrace = getStackTrace(exception);
            TextArea textArea = new TextArea();
            textArea.setText(stackTrace);

            dialogPaneContent.getChildren().addAll(label, textArea);
            alert.getDialogPane().setContent(dialogPaneContent);
        } else {
            alert.setContentText(contentText);
        }
        alert.showAndWait();
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        return s;
    }

    public static String inputTextDialog(String title, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            return dialog.getEditor().getText().trim();
        }
        return null;
    }
}
