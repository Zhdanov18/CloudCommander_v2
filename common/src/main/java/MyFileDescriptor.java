import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MyFileDescriptor implements Serializable {
    private String  name;
    private long    size;
    private Boolean directory;
    private String  parent;

    public MyFileDescriptor(Path path) {
        this.name = path.getFileName().toString().trim();
        this.directory = Files.isDirectory(path);
        this.parent = null;
        if (!isDirectory()) {
            try {
                this.size = Files.size(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MyFileDescriptor(String name, int size, Boolean directory, String parent) {
        this.name = name;
        this.size = size;
        this.directory = directory;
        this.parent = parent;
    }

    public void    setName(String name) {this.name = name; }

    public String  getName() { return name; }
    public long    getSize() { return size; }

    public Boolean isDirectory() { return directory; }
    public Boolean isParent()    { return parent != null; }
    public String  getParent()   { return parent; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyFileDescriptor that = (MyFileDescriptor) o;
        return getName().equals(that.getName()) &&
                directory.equals(that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), directory);
    }
}
