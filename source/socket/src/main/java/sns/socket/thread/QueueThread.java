package sns.socket.thread;

import org.apache.logging.log4j.Logger;
import sns.common.json.Devices;
import sns.common.json.Message;
import sns.socket.cmd.Command;
import sns.socket.cmd.ResponseCode;
import sns.socket.handler.MyChannelWSGroup;
import sns.socket.model.ClientChannel;
import sns.socket.model.event.NotificationEvent;
import sns.socket.utils.SocketService;
import sns.thread.AbstractRunable;

import java.util.List;

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
        if (notificationEvent != null && message.getTenantId() != null) {
            String prefixKey = message.getTenantId() + "_" + notificationEvent.getApp() + "_";
            String key = SocketService.getInstance().getKeyString(message.getTenantId(), notificationEvent.getApp(), notificationEvent.getUserId());
            List<ClientChannel> clientChannels = SocketService.getInstance().getClientChannelsByPrefix(prefixKey);
            if (!clientChannels.isEmpty()) {
                Message messagePost = createMessage(Command.CLIENT_RECEIVED_PUSH_NOTIFICATION, Devices.BACKEND_SOCKET_APP, notificationEvent.getPayload(), ResponseCode.RESPONSE_CODE_SUCCESS);
                for (ClientChannel clientChannel : clientChannels) {
                    if (clientChannel == null || clientChannel.getChannel() == null || !clientChannel.getChannel().isActive()) {
                        continue;
                    }
                    MyChannelWSGroup.getInstance().sendMessage(clientChannel.getChannel(), messagePost.toJson());
                }
            } else {
                LOG.debug("Not found channel: {}", key);
            }
        }
    }

//    private void handleSendNoti(Message message) {
//        NotificationEvent notificationEvent = message.getDataObject(NotificationEvent.class);
//        if (notificationEvent != null && message.getTenantId() != null) {
//            String key = SocketService.getInstance().getKeyString(message.getTenantId(), notificationEvent.getApp(), notificationEvent.getUserId());
//            ClientChannel clientChannel = SocketService.getInstance().getClientChannel(key);
//            if (clientChannel != null) {
//                PushNotiRequest pushNotiRequest = new PushNotiRequest();
//                pushNotiRequest.setCmd(notificationEvent.getCmd());
//                pushNotiRequest.setData(notificationEvent.getMessage());
//
//                Message messagePost = createMessage(Command.CLIENT_RECEIVED_PUSH_NOTIFICATION, Devices.BACKEND_SOCKET_APP, pushNotiRequest, ResponseCode.RESPONSE_CODE_SUCCESS);
//                messagePost.setCmd(Command.CLIENT_RECEIVED_PUSH_NOTIFICATION);
//                messagePost.setApp(Devices.BACKEND_SOCKET_APP);
//                messagePost.setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS);
//                messagePost.setData(pushNotiRequest);
//
//                MyChannelWSGroup.getInstance().sendMessage(clientChannel.getChannel(), messagePost.toJson());
//            } else {
//                LOG.debug("Not found channel: {}", key);
//            }
//        }
//    }

    private Message createMessage(String cmd, String app, Object data, int responseCode) {
        Message message = new Message();
        message.setCmd(cmd);
        message.setApp(app);
        message.setData(data);
        message.setResponseCode(responseCode);
        return message;
    }
}
