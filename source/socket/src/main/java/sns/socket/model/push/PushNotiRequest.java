package sns.socket.model.push;

import lombok.Data;
import sns.socket.model.ABasicPushRequest;

@Data
public class PushNotiRequest extends ABasicPushRequest {
    private String message;
    private String app;
    private Integer kind;
}
