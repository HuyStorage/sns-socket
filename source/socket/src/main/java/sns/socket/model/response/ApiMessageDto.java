package sns.socket.model.response;

import lombok.Data;

@Data
public class ApiMessageDto {
    private boolean result;
    private String code;
    private Object data;
    private String message;
}
