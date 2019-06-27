import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    protected String nick; //null if not autorized
    private Const.Node node;
    private Const.Direction direction;

    protected Thread clientTask;
    private Channel channel;

    public Node server;
    public Node client;

    private Image folderIcon  = new Image("img\\folder16.png");
    private Image arrowIcon   = new Image("img\\arrow.png");
    private Image onlineIcon  = new Image("img\\online.png");
    private Image offlineIcon = new Image("img\\offline.png");

    @FXML
    ImageView btnLoginImage;

    @FXML
    ListView<MyFileDescriptor> serverList;

    @FXML
    ListView<MyFileDescriptor> clientList;

    @FXML
    Label labelClientPath;

    @FXML
    Label labelServerPath;

    @FXML
    Label labelClientFileSize;

    @FXML
    Label labelServerFileSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.client = new Node(clientList, Paths.get(Const.userLocalHomeDir).getRoot().toString());
        this.client.setPath(Const.userLocalHomeDir);
        this.node = Const.Node.CLIENT;
        this.direction = Const.Direction.CHILD;

        serverList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        serverList.focusedProperty().addListener( (obs, oldValue, newValue) -> { selectServer(); });
        serverList.setCellFactory(param -> new ListCell<MyFileDescriptor>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(MyFileDescriptor myFileDescriptor, boolean empty) {
                super.updateItem(myFileDescriptor, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (myFileDescriptor.isDirectory()) {
                        imageView.setImage(folderIcon);
                        setGraphic(imageView);
                    } else if (myFileDescriptor.isParent()) {
                        imageView.setImage(arrowIcon);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                    setText(myFileDescriptor.getName());
                }
            }
        });
        serverList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (!isAuthorized()) { return; }
                server.update();
                refreshServerInfo();
                if (click.getClickCount() == 2) {
                    if (!(server.getItem().isParent() || server.getItem().isDirectory())) { return; }
                    try {
                        channel.writeAndFlush(new Request(Const.Action.LIST, nick, server.getItem().isDirectory() ? server.getItem().getName() : null)).sync();
                        setDirection(server.getItem().isDirectory() ? Const.Direction.CHILD : Const.Direction.PARENT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        clientList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        clientList.focusedProperty().addListener( (obs, oldValue, newValue) -> { selectClient(); });
        clientList.setCellFactory(serverList.getCellFactory());
        clientList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                client.update();
                refreshClientInfo();
                if (click.getClickCount() == 2) {
                    if (!(client.getItem().isParent() || client.getItem().isDirectory())) { return; }
                    String result = FileManager.changeDirectory(client.getItem().isDirectory() ? client.getItem().getName() : null, client.getPath());
                    if (result == null) { return; }
                    setDirection(client.getItem().isDirectory() ? Const.Direction.CHILD : Const.Direction.PARENT);
                    client.setPath(result);
                    refreshClientList();
                }
            }
        });

        MainController mainController = this;

        clientTask = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                            new ObjectDecoder(Const.maxObjectSize, ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new ClientHandler(mainController));
                    }
                });
                ChannelFuture f = b.connect(Const.host, Const.port).sync();
                channel = f.channel();
                while (!Thread.currentThread().isInterrupted()) { }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        clientTask.start();
        refreshClientList();
    }

    public void btnLogin(ActionEvent actionEvent) throws InterruptedException {
        if (!isAuthorized()) {
            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent root = loader.load();
                LoginController lc = (LoginController) loader.getController();
                lc.id = 100;
                stage.setTitle("Authorization");
                stage.setScene(new Scene(root, 400, 200));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.getIcons().add(Const.appIcon);
                stage.setResizable(false);
                stage.showAndWait();
                channel.writeAndFlush(new Request(Const.Action.AUTHORIZATION, lc.getLogin(), lc.getPassword()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setOffline();
        }
    }

    public void btnCopy(ActionEvent actionEvent) throws InterruptedException {
        if (!isAuthorized()) { return; }
        setDirection(Const.Direction.NONE);
        switch (node) {
            case SERVER:
                if (!isAuthorized() || server.getItem() == null || server.getItem().isParent()) { return; }
                if (confirmOverwrite()) {
                    channel.writeAndFlush(new Request(Const.Action.COPY, null, server.getItem().getName())).sync();
                }
                break;
            case CLIENT:
                if (client.getItem() == null || client.getItem().isParent()) { return; }
                Path path = Paths.get(client.getPath(), File.separator, client.getItem().getName());
                try {
                    if (confirmOverwrite()) {
                        Files.walkFileTree(path, new MyFileVisitor(Const.Action.COPY, path, channel));
                    }
                } catch (IOException e) {
                    DialogManager.errorMsg(Const.AlertText.COPY_ERR, path.toString(), e);
                }
                break;
        }
    }

    public void btnCreate(ActionEvent actionEvent) {
        setFocus();
        if (!isAuthorized() && isNode(Const.Node.SERVER)) { return; }

        String folder = DialogManager.inputTextDialog("Create directory", null, null);

        if (folder == null) return;

        String folderPath = String.join(File.separator, client.getPath(), folder);
        switch (node) {
            case CLIENT:
                if (FileManager.createDirectory(Paths.get(client.getPath(), File.separator, folder), null)) {
                    client.setCreateItem(folder);
                    setDirection(Const.Direction.CREATE);
                    refreshClientList();
                } else {
                    DialogManager.errorMsg(Const.AlertText.CREATE_DIR_ERR, folderPath, null);
                }
                break;
            case SERVER:
                channel.writeAndFlush(new Request(Const.Action.CREATE, null, folder));
                server.setCreateItem(folder);
                setDirection(Const.Direction.CREATE);
                break;
        }
    }

    public void btnRemove(ActionEvent actionEvent) throws InterruptedException {
        switch (node) {
            case SERVER:
                if (!isAuthorized() || server.getItem() == null || server.getItem().isParent()) { return; }
                if (DialogManager.confirmMsg(Const.AlertText.DELETE, String.join(File.separator, server.getPathForDisplay(), server.getItem().getName()))) {
                    setDirection(Const.Direction.REMOVE);
                    channel.writeAndFlush(new Request(Const.Action.REMOVE, null, server.getItem().getName())).sync();
                }
                break;
            case CLIENT:
                if (client.getItem() == null || client.getItem().isParent()) { return; }
                Path path = Paths.get(String.join(File.separator, client.getPath(), client.getItem().getName()));
                if (DialogManager.confirmMsg(Const.AlertText.DELETE, path.toString())) {
                    try {
                        Files.walkFileTree(path, new MyFileVisitor(Const.Action.REMOVE, path, channel));
                    } catch (IOException e) {
                        DialogManager.errorMsg(Const.AlertText.DELETE_ERR, path.toString(), e);
                    }
                    setDirection(Const.Direction.REMOVE);
                    refreshClientList();
                }
                break;
        }
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void refreshClientList() {
        updateUI(() -> {
            clientList.getItems().clear();
            clientList.getItems().addAll(FileManager.getFileList(client.getPath(), client.getRoot()));
            client.selectItem(direction);
            refreshClientInfo();
        });
    }

    public void refreshServerList(FolderMessage msg) {
        updateUI(() -> {
            serverList.getItems().clear();
            serverList.getItems().addAll(msg.getData());
            server.selectItem(direction);
            refreshServerInfo();
        });
    }

    public void refreshList() throws InterruptedException {
        setDirection(Const.Direction.CHILD);
        if (!isAuthorized() || isNode(Const.Node.CLIENT)) {
            refreshClientList();
            return;
        }
        channel.writeAndFlush(new Request(Const.Action.LIST, nick, server.getPath())).sync();
    }

    private void refreshClientInfo() {
        setFocus();
        if (client.getItem() == null) {
            return;
        }
        labelClientPath.setText(client.getPath());
        if (client.getItem().isParent() || client.getItem().isDirectory()) {
            labelClientFileSize.setText("");
        } else {
            labelClientFileSize.setText(String.format("%,d", client.getItem().getSize()));
        }
    }

    private void refreshServerInfo() {
        setFocus();
        if (!isAuthorized()) {
            labelServerPath.setText("");
            labelServerFileSize.setText("");
            return;
        }
        labelServerPath.setText(nick + ":" + server.getPathForDisplay());
        if (server.getItem() == null) {
            return;
        }
        if (server.getItem().isParent() || server.getItem().isDirectory()) {
            labelServerFileSize.setText("");
        } else {
            labelServerFileSize.setText(String.format("%,d", server.getItem().getSize()));
        }
    }

    private boolean confirmOverwrite() {
        switch (node) {
            case SERVER:
                if (!clientList.getItems().contains(server.getItem())) { return true; }
                if (DialogManager.confirmMsg(Const.AlertText.REWRITE, server.getItem().getName())) { return true; }
                break;
            case CLIENT:
                if (!serverList.getItems().contains(client.getItem())) { return true; }
                if (DialogManager.confirmMsg(Const.AlertText.REWRITE, client.getItem().getName())) { return true; }
                break;
        }
        return false;
    }

    protected void setOnline(String nick) {
        this.nick = nick;
        setDirection(Const.Direction.CHILD);
        server = new Node(serverList, String.join(File.separator, Const.storageRoot, nick));
        btnLoginImage.setImage(onlineIcon);
    }

    protected void setOffline() throws InterruptedException {
        if (DialogManager.confirmMsg(Const.AlertText.DISCONNECT, nick)) {
            channel.writeAndFlush(new Request(Const.Action.DISCONNECT, nick, null)).sync();
            nick = null;
            serverList.getItems().clear();
            btnLoginImage.setImage(offlineIcon);
            refreshServerInfo();
        }
    }

    private void setFocus() {
        switch (node) {
            case CLIENT:
                clientList.requestFocus();
                break;
            case SERVER:
                serverList.requestFocus();
                break;
        }
    }

    private boolean isAuthorized() { return nick != null; }
    private boolean isNode(Const.Node node) {return this.node == node; }

    private void selectClient() { this.node = Const.Node.CLIENT; }
    private void selectServer() { this.node = Const.Node.SERVER; }

    protected void setDirection(Const.Direction direction) { this.direction = direction; }

}
