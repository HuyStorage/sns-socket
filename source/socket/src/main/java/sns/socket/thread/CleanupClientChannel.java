package sns.socket.thread;

import org.apache.logging.log4j.Logger;
import sns.socket.utils.SocketService;

import java.util.TimerTask;

public class CleanupClientChannel extends TimerTask {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CleanupClientChannel.class);

    @Override
    public void run() {
        LOG.info("#############============>XXXX Clean up key");
        SocketService.getInstance().scanAndRemoveChannel();
        LOG.info("#############============>XXXX Clean up key Done. Size: " + SocketService.getInstance().countChannelOrder());
    }
}
