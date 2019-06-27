import javafx.scene.image.Image;

public class Const {
    public enum Node      { CLIENT, SERVER }
    public enum Action    { AUTHORIZATION, COPY, CREATE, DISCONNECT, LIST, REMOVE }
    public enum Direction { CHILD, CREATE, PARENT, NONE, REMOVE }
    public enum AlertText {
        CREATE_DIR_ERR ("Error",      "The directory is not created"),
        DELETE_ERR     ("Error",      "An error occurred during deletion"),
        DELETE_DIR_ERR ("Error",      "The directory is not deleted"),
        DELETE_FILE_ERR("Error",      "The file is not deleted"),
        COPY_ERR       ("Error",      "An error occurred while copying"),
        DELETE         ("Delete",     "Are you sure want to delete?"),
        DISCONNECT     ("Disconnect", "Are you sure want to disconnect?"),
        REWRITE        ("Rewrite",    "Your data will be overwritten. Are you sure?");

        private String title;
        private String headerText;

        public String getTitle() { return title; }
        public String getHeaderText() { return headerText; }

        AlertText(String title, String headerText) {
            this.title = title;
            this.headerText = headerText;
        }
    }

    public static final String storageRoot      = "server_storage";
    public static final String userLocalHomeDir = System.getProperty("user.dir");
    public static final Image  appIcon          = new Image("img\\cloud.png");
    public static final String linkToParent     = "[ .. ]";

    public static final String host   = "localhost";
    public static final int    port   = 8189;
    public static final int    buffer = 1 * 1024 * 1024;

    public static final int maxObjectSize = 3 * buffer;
}
