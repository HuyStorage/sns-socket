package sns.socket.model.request;

import lombok.Data;
import sns.socket.model.ABasicRequest;

@Data
public class ClientInfoRequest extends ABasicRequest {
    private String app;
}
