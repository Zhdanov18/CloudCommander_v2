import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private String nick;
    private String path;

    public MainHandler(ChannelHandlerContext ctx, String nick) {
        super();
        this.nick = nick;

        File clientRoot = new File(getClientRoot());
        if (!clientRoot.exists()) { clientRoot.mkdir(); }
        this.path = getClientRoot();

        ctx.writeAndFlush(new FolderMessage(path, getServerFileList()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) { return; }
            if (msg instanceof Request) {
                Request request = (Request) msg;
                Path file = Paths.get(path, ((request.getArg(0) != null) ? File.separator + request.getArg(0) : ""), ((request.getArg(1) != null) ? File.separator + request.getArg(1) : ""));
                switch (request.getAction()) {
                    case COPY:
                        try {
                            Files.walkFileTree(file, new MyFileVisitor(Const.Action.COPY, file, ctx.channel()));
                        } catch (IOException e) {
                            ctx.writeAndFlush(new ErrorMessage(e, Const.AlertText.COPY_ERR, file.toString()));
                        }
                        break;
                    case CREATE:
                        if (FileManager.createDirectory(file, null)) {
                            ctx.writeAndFlush(new FolderMessage(path, getServerFileList()));
                        } else {
                            ctx.writeAndFlush(new ErrorMessage(null, Const.AlertText.CREATE_DIR_ERR, file.toString()));
                        }
                        break;
                    case LIST:
                        String result = FileManager.changeDirectory(request.getArg(1), path);
                        if (result != null) {
                            path = result;
                        }
                        ctx.writeAndFlush(new FolderMessage(path, getServerFileList()));
                        break;
                    case REMOVE:
                        try {
                            Files.walkFileTree(file, new MyFileVisitor(Const.Action.REMOVE, file, null));
                        } catch (IOException e) {
                            ctx.writeAndFlush(new ErrorMessage(e, Const.AlertText.DELETE_ERR, file.toString()));
                        }
                        ctx.writeAndFlush(new FolderMessage(path, getServerFileList()));
                        break;
                }
            }
            if (msg instanceof FileMessage) {
                FileManager.writeFileMsg((FileMessage) msg, path);
                ctx.writeAndFlush(new FolderMessage(path, getServerFileList()));
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public String getClientRoot() {
        return String.join(File.separator, Const.storageRoot, this.nick);
    }

    public List<MyFileDescriptor> getServerFileList() {
        return FileManager.getFileList(path, getClientRoot());
    }
}
