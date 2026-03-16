package sns.socket.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import sns.socket.constant.NotiConstant;
import sns.socket.constant.RedisConstant;
import sns.socket.redis.tjedis.TJedis;
import sns.socket.redis.tjedis.TJedisAbstractPool;
import sns.socket.redis.tjedis.TJedisPool;
import sns.socket.redis.tjedis.TJedisSentinelPool;
import sns.socket.utils.SocketService;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * https://github.com/redisson/redisson
 * https://github.com/redisson/redisson/wiki/Table-of-Content
 * <p>
 * Docker: https://hub.docker.com/r/bitnami/redis-sentinel
 */
public class RedisService {
    private final Logger log = LogManager.getLogger(RedisService.class);
    private static RedisService instance;
    private final TJedis iRedis;
    private final Integer TWO_HOURS = 7200;
    private final SimpleDateFormat DATE_FORMAT;


    private RedisService() {
        int maxSize = SocketService.getInstance().getIntResource("server.redis.threads.size");
        int redisType = SocketService.getInstance().getIntResource("redis.type");
        String password = SocketService.getInstance().getStringResource("redis.password");

        TJedisAbstractPool pool;
        if (redisType == 2) {
            String[] sentinelHosts = SocketService.getInstance().getStringArray("redis.sentinel.host");
            String masterName = SocketService.getInstance().getStringResource("redis.master.name");
            pool = new TJedisSentinelPool(sentinelHosts, masterName, maxSize, password);
        } else {
            String standAloneHost = SocketService.getInstance().getStringResource("redis.host");
            pool = new TJedisPool(standAloneHost, maxSize, password);
        }
        this.iRedis = new TJedis(pool);
        this.DATE_FORMAT = new SimpleDateFormat(NotiConstant.DATE_TIME_FORMAT);
    }

    public Jedis getJedis() {
        return iRedis.getJedis();
    }

    public static RedisService getInstance() {
        if (instance == null) {
            instance = new RedisService();
        }
        return instance;
    }

    public void startRedis() {
        iRedis.startRedis();
    }

    private boolean isFieldExistAndHasValue(Jedis jedis, String key, String field, String expectedValue) {
        if (jedis.hexists(key, field)) {
            String fieldValue = jedis.hget(key, field);
            return fieldValue != null && fieldValue.equals(expectedValue);
        }
        return false;
    }

    public Map<String, String> hashGetByKey(Jedis jedis, String key) {
        return jedis.hgetAll(key);
    }

    public void handleUpdateChannel(String key, String channelId) {
        Jedis jedis = getJedis();
        String serverId = SocketService.getInstance().getStringResource("server.id");
        try {
            jedis.hset(key, RedisConstant.FIELD_SERVER_ID, serverId);
            jedis.hset(key, RedisConstant.FIELD_CLIENT_SOCKET_ID, channelId);
            jedis.expire(key, 60); // 1 minute
        } catch (Exception e) {
            log.error("Error updating channel in Redis: {}", e.getMessage(), e);
        } finally {
            jedis.close();
        }
    }


    public void deleteByKey(String key) {
        Jedis jedis = getJedis();
        try {
            jedis.del(key);
        } catch (Exception e) {
            log.error("Error deleting key {}: {}", key, e.getMessage(), e);
        } finally {
            jedis.close();
        }
    }
}