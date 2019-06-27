import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String  path;
    private String  filename;
    private byte[]  data;
    private boolean firstPacket;

    public String  getPath()       { return path; }
    public String  getFilename()   { return filename; }
    public byte[]  getData()       { return data; }
    public int     length()        { return data.length; }
    public boolean isFirstPacket() { return firstPacket; }

    public void setParameters(byte[] data, boolean firstPacket) {
        this.data = data;
        this.firstPacket = firstPacket;
    }

    public FileMessage(String path, Path filename) {
        this.path = path;
        this.filename = filename.getFileName().toString();
    }

    public FileMessage(String path, Path filename, byte[] data, boolean firstPacket) {
        this.path = path;
        this.filename = filename.getFileName().toString();
        this.data = data;
        this.firstPacket = firstPacket;
    }
}
