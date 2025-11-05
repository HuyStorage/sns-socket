package sns.socket.model.response;

import lombok.Data;
import sns.socket.model.ABasicModel;

@Data
public class DeviceDto extends ABasicModel {
    private String posId;
    private Integer type;
    private String tenant;
    private String time;
    private String session;
}
