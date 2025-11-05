package sns.socket.model;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class ClientChannel {
    private Channel channel;
    private Long time;
}
