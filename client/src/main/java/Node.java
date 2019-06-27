import javafx.scene.control.ListView;

import java.io.File;

public class Node {
    private MyFileDescriptor item;
    private int index;
    private String path;
    private String root;
    private ListView<MyFileDescriptor> list;
    private String createItem;

    public Node(ListView<MyFileDescriptor> list, String root) {
        this.list = list;
        this.root = root;
        this.path = root;
    }

    public MyFileDescriptor getItem() { return item; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getRoot() { return root; }

    public void setCreateItem(String createItem) {
        this.createItem = createItem;
    }

    public void update() {
        this.item = list.getSelectionModel().getSelectedItem();
        this.index = list.getSelectionModel().getSelectedIndex();
    }

    private void locateItem(String name, boolean isDirectory) {
        for (MyFileDescriptor d : list.getItems())
            if (d.getName().equals(name) && d.isDirectory() == isDirectory)
                list.getSelectionModel().select(list.getItems().indexOf(d));
    }

    public void selectItem(Const.Direction direction) {
        switch (direction) {
            case CHILD:
                list.getSelectionModel().selectFirst();
                break;
            case CREATE:
                locateItem(createItem, true);
                break;
            case PARENT:
                //выбран элемент [..], в поле parent - путь к данному каталогу
                locateItem(item.getParent().substring(item.getParent().lastIndexOf(File.separator) + 1), true);
                break;
            case REMOVE:
                if (index >= list.getItems().size()) {
                    list.getSelectionModel().selectLast();
                } else {
                    list.getSelectionModel().select(index);
                }
                break;
            case NONE:
                this.index = list.getItems().indexOf(item);
                list.getSelectionModel().select(index);
                break;
        }
        if (direction != Const.Direction.NONE) {
            update();
        }
        list.scrollTo(index);
    }

    public String getPathForDisplay() {
        if (path.equals(root)) {
            return "";
        } else {
            return path.substring(root.length());
        }
    }
}
