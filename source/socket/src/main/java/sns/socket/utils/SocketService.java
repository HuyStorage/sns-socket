package sns.socket.utils;

import lombok.Getter;
import org.apache.logging.log4j.Logger;
import sns.common.utils.ConfigurationService;
import sns.socket.jwt.UserSession;
import sns.socket.model.ClientChannel;
import sns.thread.WorkerPool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class SocketService {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(SocketService.class);
    private final int ONE_MINUTE = 60 * 1000;
    // key: {tenantId}_{userKind}_{userId}
    private final ConcurrentHashMap<String, ClientChannel> userChannel = new ConcurrentHashMap<>();
    private static SocketService instance = null;
    private final ConfigurationService config;
    private final WorkerPool workerPool;

    @Getter
    private final Timer timer = new Timer(false);

    private SocketService() {
        config = new ConfigurationService("configuration.properties");
        workerPool = new WorkerPool(getIntResource("server.socket.threads.size"), getIntResource("server.socket.queue.size"), (r, executor) -> {
        });
    }

    public static SocketService getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    public String getStringResource(String key) {
        return config.getString(key);
    }

    public void setStringResource(String key, String value) {
        if (config.containsKey(key)) {
            config.setProperty(key, value);
            try {
                config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public WorkerPool getWorkerPool() {
        return workerPool;
    }

    public int getIntResource(String key) {

        return config.getInt(key);
    }

    public String[] getStringArray(String key) {
        return config.getStringArray(key);
    }

    public void addClientChannel(String userId, ClientChannel channelId) {
        userChannel.put(userId, channelId);
        LOG.info("[SocketService] >>> addClientChannel: userId=" + userId + ", channelId=" + channelId);
    }

    public void removeClientChannel(UserSession userSession) {
        String key = getKeyString(userSession.getTenantName(), userSession.getApp(), userSession.getId());
        userChannel.remove(key);
    }

    public ClientChannel getClientChannel(String userId) {
        if (userChannel.containsKey(userId)) {
            return userChannel.get(userId);
        }
        return null;
    }

    public List<ClientChannel> getClientChannelsByPrefix(String prefix) {
        List<ClientChannel> result = new ArrayList<>();
        userChannel.forEach((key, value) -> {
            if (key != null && key.startsWith(prefix) && value != null) {
                result.add(value);
            }
        });
        return result;
    }

    public void scanAndRemoveChannel() {
        Iterator<String> keys = userChannel.keySet().stream().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ClientChannel value = userChannel.get(key);
            // > 1 phut thi del no di
            if (System.currentTimeMillis() - value.getTime() > ONE_MINUTE) {
                userChannel.remove(key);
            } else {
                LOG.debug("Key exists: {}", key);
            }
        }
    }

    public int countChannelOrder() {
        return userChannel.size();
    }

    public String getKeyString(String tenantId, String app, Long userId) {
        return tenantId + "_" + app + "_" + userId;
    }

    public String getKeyString(UserSession userSession) {
        return userSession.getTenantName() + "_" + userSession.getApp() + "_" + userSession.getId();
    }
}
