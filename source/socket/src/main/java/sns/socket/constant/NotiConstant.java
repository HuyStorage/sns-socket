package sns.socket.constant;

import io.netty.util.AttributeKey;

public class NotiConstant {
    public static final int NOTIFICATION_KIND_ONE = 1;
    public static final int NOTIFICATION_KIND_ALL = 2;

    public static final String GRANT_TYPE_PASSWORD = "password";

    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static final int USER_KIND_ADMIN = 1;
    public static final int USER_KIND_CUSTOMER = 2;
    public static final int USER_KIND_EMPLOYEE = 3;

    public static final String APP_MASTER = "MASTER";
    public static final String APP_TENANT = "TENANT";
    public static final String APP_USER = "USER";

    public static final AttributeKey<String> TOKEN = AttributeKey.valueOf("token");
}
