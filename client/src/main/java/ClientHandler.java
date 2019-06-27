import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private MainController fx;

    public ClientHandler(MainController mainController) {
        this.fx = mainController;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) { return; }
            if (msg instanceof FileMessage) {
                FileManager.writeFileMsg((FileMessage) msg, fx.client.getPath());
                fx.refreshClientList();
            }
            if (msg instanceof AuthMessage) {
                AuthMessage authMessage = (AuthMessage) msg;
                String nick = authMessage.getName();
                if (nick != null) {
                    fx.setOnline(nick);
                }
            }
            if (msg instanceof FolderMessage) {
                FolderMessage fm = (FolderMessage) msg;
                fx.refreshServerList(fm);
                fx.server.setPath(fm.getPath());
            }
            if (msg instanceof ErrorMessage) {
                ErrorMessage em = (ErrorMessage) msg;
                fx.updateUI(() -> {
                    DialogManager.errorMsg(em.getAlertText(), fx.server.getPathForDisplay() + File.separator + em.getContentText(), em.getException());
                });
            }
            if (msg instanceof Request) {
                Request request = (Request) msg;
                switch (request.getAction()) {
                    case CREATE:
                        Path path = Paths.get(fx.client.getPath(), File.separator, ((request.getArg(0) != null) ? request.getArg(0) + File.separator : ""), request.getArg(1));
                        if (FileManager.createDirectory(path, null) && path.equals(Paths.get(fx.client.getPath()))) {
                            fx.setDirection(Const.Direction.NONE);
                            fx.refreshClientList();
                        }
                        break;
                }
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
}
