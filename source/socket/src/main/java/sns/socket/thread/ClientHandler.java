package sns.socket.thread;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sns.common.json.Message;
import sns.socket.cmd.ResponseCode;
import sns.socket.handler.MyChannelWSGroup;
import sns.socket.jwt.UserSession;
import sns.socket.model.ClientChannel;
import sns.socket.model.response.ClientInfoResponse;
import sns.socket.utils.SocketService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientHandler {
    private static final Logger LOG = LogManager.getLogger(ClientHandler.class);
    private static ClientHandler instance = null;

    private ClientHandler() {

    }

    public static ClientHandler getInstance() {
        if (instance == null) {
            instance = new ClientHandler();
        }
        return instance;
    }

    private void sendErrorMsg(ChannelHandlerContext channelHandlerContext, Message oldRequest, String msg) {
        Message response = new Message();
        response.setCmd(oldRequest.getCmd());
        response.setMsg(msg);
        response.setResponseCode(ResponseCode.RESPONSE_CODE_ERROR);
        MyChannelWSGroup.getInstance().sendMessage(channelHandlerContext.channel(), response.toJson());
    }


    public void handlePing(ChannelHandlerContext channelHandlerContext, Message message) {
        UserSession userSession = UserSession.fromToken(message.getToken());
        message.setToken(null);
        message.setChannelId(null);
        if (userSession != null) {
            handleCacheClientSession(userSession, channelHandlerContext);
            message.setData(new ClientInfoResponse());
            message.setMsg("Ping success with channel ID: " + SocketService.getInstance().getKeyString(userSession));
            message.setChannelId(SocketService.getInstance().getKeyString(userSession));
            message.setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS);
            MyChannelWSGroup.getInstance().sendMessage(channelHandlerContext.channel(), message.toJson());
            LOG.info("[Client Ping] Ping success with channel ID: {}", SocketService.getInstance().getKeyString(userSession));
        } else {
            LOG.info("[Client Ping] Token invalid");
            sendErrorMsg(channelHandlerContext, message, "Token invalid");
        }
    }

    public void handleVerifyToken(ChannelHandlerContext channelHandlerContext, Message message) {
        UserSession userSession = UserSession.fromToken(message.getToken());
        message.setToken(null);
        message.setChannelId(null);
        if (userSession != null) {
            handleCacheClientSession(userSession, channelHandlerContext);
            message.setData(new ClientInfoResponse());
            message.setMsg("Verify success with channel ID: " + SocketService.getInstance().getKeyString(userSession));
            message.setChannelId(SocketService.getInstance().getKeyString(userSession));
            message.setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS);
            MyChannelWSGroup.getInstance().sendMessage(channelHandlerContext.channel(), message.toJson());
            LOG.info("[Client Verify] Verify success with channel ID: {}", SocketService.getInstance().getKeyString(userSession));
        } else {
            LOG.info("[Client Verify Token] Token invalid");
            sendErrorMsg(channelHandlerContext, message, "Token invalid");
        }
    }

    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCacheClientSession(UserSession userSession, ChannelHandlerContext channelHandlerContext) {
        String keyString = SocketService.getInstance().getKeyString(userSession);

        ClientChannel channel = SocketService.getInstance().getClientChannel(keyString);
        if (channel != null) {
            channel.setTime(System.currentTimeMillis());
            channel.setChannel(channelHandlerContext.channel());
        } else {
            ClientChannel clientChannel = new ClientChannel();
            clientChannel.setChannel(channelHandlerContext.channel());
            clientChannel.setTime(System.currentTimeMillis());
            SocketService.getInstance().addClientChannel(keyString, clientChannel);
        }
    }
}
