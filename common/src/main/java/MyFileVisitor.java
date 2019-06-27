import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileVisitor extends SimpleFileVisitor<Path> {

    private Const.Action action;
    private Path source;
    private Channel channel;

    public MyFileVisitor(Const.Action action, Path source, Channel channel) {
        this.action  = action;
        this.source  = source;
        this.channel = channel;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        switch (action) {
            case REMOVE:
                Files.deleteIfExists(file);
                break;
            case COPY:
                FileManager.sendFileMsg(new FileMessage(getRelativePath(file), file), channel);
                break;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
    {
        switch (action) {
            case REMOVE:
                break;
            case COPY:
                String file = dir.getFileName().toString();
                if (dir.equals(source)) {
                    channel.writeAndFlush(new Request(Const.Action.CREATE, null, file));
                } else {
                    channel.writeAndFlush(new Request(Const.Action.CREATE, getRelativePath(dir), file));
                }
                break;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
        switch (action) {
            case REMOVE:
                Files.deleteIfExists(path);
                break;
            case COPY:
                break;
        }
        return FileVisitResult.CONTINUE;
    }

    private String getRelativePath(Path path) {
        if (source.equals(path)) { return ""; }
        int startIndex = source.toString().lastIndexOf(File.separator) + 1;
        return path.toString().substring(startIndex, path.toString().lastIndexOf(File.separator));
    }
}
