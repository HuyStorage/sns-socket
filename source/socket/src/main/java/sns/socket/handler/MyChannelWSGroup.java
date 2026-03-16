package sns.socket.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sns.socket.model.push.PushNotiRequest;
import sns.socket.utils.SocketService;

public class MyChannelWSGroup {
    private final static Logger LOG = LogManager.getLogger(MyChannelWSGroup.class.getName());
    private static MyChannelWSGroup instance = null;
    private static final Object lock = new Object();

    private MyChannelWSGroup() {

    }

    public synchronized static MyChannelWSGroup getInstance() {
        if (instance == null) {
            synchronized (lock) {
                instance = new MyChannelWSGroup();
            }
        }
        return instance;
    }

    public void sendMessage(Channel channel, String message) {
        if (channel != null && channel.isActive()) {
            try {
                LOG.info("[Socket] >>> Sending notification {}", message);
                channel.writeAndFlush(new TextWebSocketFrame(message));
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            LOG.error("[Socket] >>> Sending notification failed -> channel null");
        }
    }

    public void sendMessage(Channel channel, PushNotiRequest msg) {


        if (channel != null && channel.isActive()) {
            try {
                LOG.info("[Socket] >>> Sending notification {}", msg.toJson());
                channel.writeAndFlush(new TextWebSocketFrame(msg.toJson()));
            } catch (Exception e) {
                LOG.error("WebSocket send failed, fallback to push notification: {}", e.getMessage(), e);
            }
        }
    }
}
