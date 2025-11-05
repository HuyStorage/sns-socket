package sns.socket.model.event;

import lombok.Data;
import sns.socket.model.ABasicModel;

@Data
public class NotificationEvent extends ABasicModel {
    private String message;
    private Long appId;
    private Long channelId;
    private String socketId;
}
