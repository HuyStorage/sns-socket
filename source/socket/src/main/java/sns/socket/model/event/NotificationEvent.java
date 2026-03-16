package sns.socket.model.event;

import lombok.Data;
import sns.socket.model.ABasicModel;

@Data
public class NotificationEvent extends ABasicModel {
    private String app; // MASTER, TENANT, USER
    private Long userId;
    private Object payload;
}
