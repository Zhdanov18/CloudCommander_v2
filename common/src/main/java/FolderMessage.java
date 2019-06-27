import java.util.List;

public class FolderMessage extends AbstractMessage {
    private String path;
    private List<MyFileDescriptor> data;

    public FolderMessage(String path, List<MyFileDescriptor> data) {
        this.path = path;
        this.data = data;
    }

    public String getPath() { return path; }

    public List<MyFileDescriptor> getData() { return data;  }
}
