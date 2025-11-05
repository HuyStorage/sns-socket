package sns.socket.thread;

import org.apache.logging.log4j.Logger;
import sns.common.json.Devices;
import sns.common.json.Message;
import sns.socket.cmd.Command;
import sns.socket.cmd.ResponseCode;
import sns.socket.handler.MyChannelWSGroup;
import sns.socket.model.ClientChannel;
import sns.socket.model.event.NotificationEvent;
import sns.socket.model.push.PushNotiRequest;
import sns.socket.redis.RedisService;
import sns.socket.utils.SocketService;
import sns.thread.AbstractRunable;


public class QueueThread extends AbstractRunable {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(QueueThread.class);
    private String data;

    public QueueThread(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void run() {
        try {
            LOG.debug("BACKEND CALL =====> " + data);
            Message message = Message.fromJson(data, Message.class);
            if (message != null) {

                switch (message.getApp()) {
                    case Devices.BACKEND_APP:
                        handleBackendApp(message);
                        break;
                    default:
                        LOG.info("NO sub command process with: " + message.getSubCmd());
                }
            } else {
                LOG.error("message null or channel id null");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void handleBackendApp(Message message) {
        switch (message.getCmd()) {
            case Command.CMD_BROADCAST:
                handleBroadcastNoti(message);
                break;
            default:
                LOG.info("NO sub command process with: " + message.getSubCmd());
        }
    }

    /**
     * handlePostNoti
     * {
     * "cmd": "BACKEND_POST_NOTIFICATION",
     * "app": "BACKEND_APP",
     * "data": {
     * "kind": 1,
     * "app": "ELMS",
     * "message": "Noi dung msg here",
     * "userId": 1234,
     * "cmd": "BACKEND_POST_NOTIFICATION"
     * }
     * }
     */
    private void handleBroadcastNoti(Message message) {
        NotificationEvent notificationEvent = message.getDataObject(NotificationEvent.class);
        if (notificationEvent != null && notificationEvent.getAppId() != null && notificationEvent.getChannelId() != null) {
            ClientChannel clientChannel = SocketService.getInstance().getClientChannel(notificationEvent.getSocketId());
            if (clientChannel != null) {
                PushNotiRequest pushNotiRequest = new PushNotiRequest();
                pushNotiRequest.setApp(notificationEvent.getAppId().toString());
                pushNotiRequest.setMessage(notificationEvent.getMessage());

                Message messagePost = new Message();
                messagePost.setCmd(Command.CLIENT_RECEIVED_PUSH_NOTIFICATION);
                messagePost.setApp(Devices.BACKEND_SOCKET_APP);
                messagePost.setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS);
                messagePost.setData(pushNotiRequest);

                MyChannelWSGroup.getInstance().sendMessage(clientChannel.getChannel(), message.toJson());
            } else {
                LOG.debug("Not found channel: {}", notificationEvent.getChannelId());
            }
        }
    }

    private Message createMessage(String cmd, String app, Object data, String msg, int responseCode) {
        Message message = new Message();
        message.setCmd(cmd);
        message.setApp(app);
        message.setData(data);
        message.setMsg(msg);
        message.setResponseCode(responseCode);
        return message;
    }
}
