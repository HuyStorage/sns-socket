package sns.socket.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import sns.socket.constant.NotiConstant;
import sns.socket.utils.SocketService;

@Data
public class UserSession {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(UserSession.class);

    private Long id;
    private Integer kind;
    private String tenantInfo;
    private String tenantName;
    private String app;

    public static UserSession fromToken(String token) {
        String publicKey = SocketService.getInstance().getStringResource("server.public.key");
        try {
            Algorithm algorithm = Algorithm.HMAC256(publicKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .acceptLeeway(1) // 1 sec for nbf and iat
                    .acceptExpiresAt(5) // 5 secs for exp
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            Long userId = decodedJWT.getClaim("user_id").asLong();
            Integer userKind = decodedJWT.getClaim("user_kind").asInt();
            String tenantInfo = decodedJWT.getClaim("tenant_info").asString();
            String tenantName = decodedJWT.getClaim("tenant_name").asString();
            String grantType = decodedJWT.getClaim("grant_type").asString();
            if (StringUtils.isBlank(grantType)) {
                return null;
            }
            UserSession userSession = new UserSession();
            if (StringUtils.isNoneBlank(tenantInfo)) {
                userSession.setTenantInfo(tenantInfo);
            }
            if (StringUtils.isNoneBlank(tenantName)) {
                userSession.setTenantName(tenantName);
            }
            if (userId != null && userKind != null) {
                userSession.setId(userId);
                userSession.setKind(userKind);
                userSession.setApp(userSession.handleApp(userKind, tenantName));
                return userSession;
            }
            return null;
        } catch (Exception e) {
            LOG.error("RSA key: " + publicKey);
            LOG.error("verifierJWT>>" + e.getMessage());
            return null;
        }
    }

    private String handleApp(Integer userKind, String tenantName) {
        if (userKind == null) {
            return null;
        }
        if (userKind == NotiConstant.USER_KIND_ADMIN) {
            return NotiConstant.APP_MASTER;
        }
        if (userKind == NotiConstant.USER_KIND_CUSTOMER) {
            return StringUtils.isBlank(tenantName) ? NotiConstant.APP_MASTER : NotiConstant.APP_TENANT;
        }
        if (userKind == NotiConstant.USER_KIND_EMPLOYEE) {
            return NotiConstant.APP_TENANT;
        }
        return NotiConstant.APP_USER;
    }
}
