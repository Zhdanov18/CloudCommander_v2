import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedNioFile;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileManager {

    public static void sendFileMsg(AbstractMessage msg, Channel channel) {
        FileMessage fileMessage = (FileMessage) msg;

        String file = fileMessage.getFilename();
        int sizeBuffer = 0;
        try {
            sizeBuffer = (Const.buffer <= Files.size(Paths.get(file))) ? Const.buffer : (int) Files.size(Paths.get(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[sizeBuffer];

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), sizeBuffer)) {
            int frame;
            boolean first = true;

            while ((frame = in.read(data)) != -1) {
                fileMessage.setParameters(frame < sizeBuffer ? Arrays.copyOf(data, frame) : data, first);
                channel.writeAndFlush(fileMessage);
                first = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFileMsg(FileMessage fm, String currentPath) {
        String path = String.join(File.separator, currentPath, ((fm.getPath() != null) ? fm.getPath() + File.separator : ""), fm.getFilename());

        byte[] data = fm.getData();

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path, !fm.isFirstPacket()), fm.length())) {
            out.write(data);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<MyFileDescriptor> getFileList(String path, String root) {
        List<MyFileDescriptor> data = new ArrayList<>();
        File dir = new File(path);
        for (File f : dir.listFiles())
            data.add(new MyFileDescriptor(Paths.get(f.getAbsolutePath())));
        data.sort((o1, o2) -> {
            if (o1.isDirectory() && !o2.isDirectory()) {
                return -1;
            } else if (!o1.isDirectory() && o2.isDirectory()) {
                return 1;
            } else {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        if (!path.equals(root)) {
            data.add(0, new MyFileDescriptor(Const.linkToParent, 0, false, path));
        }
        return data;
    }

    public static boolean createDirectory(Path path, Exception exception) {
        boolean create = false;
        try {
            if (Files.exists(Files.createDirectory(path))) {
                create = true;
            }
        } catch (IOException e) {
            if (exception != null) exception = e;
        } finally {
            return create;
        }
    }

    public static String changeDirectory(String directory, String path) {
        String newDirectory = (directory != null) ? String.join(File.separator, path, directory) : path.substring(0, path.lastIndexOf(File.separator));
        if (Files.exists(Paths.get(newDirectory))) {
            return newDirectory;
        }
        return null;
    }
}
