package sns.socket.model.push;

import lombok.Data;
import sns.socket.model.ABasicPushRequest;

@Data
public class PushNotiRequest extends ABasicPushRequest {
    private String cmd;
    private String subCmd;
    private String data;
}
