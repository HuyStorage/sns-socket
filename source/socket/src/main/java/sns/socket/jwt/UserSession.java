package sns.socket.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import sns.socket.utils.SocketService;

@Data
public class UserSession {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(UserSession.class);

    private Long appId;
    private Long channelId;

    public static UserSession fromToken(String token) {
        if (StringUtils.isBlank(token)) return null;

        final String secret = SocketService.getInstance().getStringResource("server.public.key"); // HMAC secret
        try {
            JWTVerifier verifier = JWT
                    .require(Algorithm.HMAC256(secret))
                    .acceptLeeway(1)      // nbf, iat
                    .acceptExpiresAt(5)   // exp
                    .build();

            DecodedJWT jwt = verifier.verify(token);

            Long appId = jwt.getClaim("app_id").asLong();
            Long channelId = jwt.getClaim("channel_id").asLong();
            if (appId == null || channelId == null) {
                LOG.warn("Invalid JWT token, missing required claims: app_id, channel_id");
                return null;
            }

            UserSession us = new UserSession();
            us.setAppId(appId);
            us.setChannelId(channelId);
            return us;

        } catch (Exception e) {
            LOG.warn("JWT verification failed: {}", e.toString());
            return null;
        }
    }
}
