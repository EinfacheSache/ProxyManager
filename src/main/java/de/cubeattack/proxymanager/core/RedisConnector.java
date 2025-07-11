package de.cubeattack.proxymanager.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisAccessControlException;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RedisConnector {

    private JedisPool jedisPool = null;
    private final Map<String,String> localCache = new ConcurrentHashMap<>();


    public RedisConnector() {
        if (!Config.connectRedis() || !Core.isMinecraftServer()) {
            Core.info("Redis - no connection established (Redis disabled or not a Minecraft server)");
            return;
        }

        // Attempt external Redis connection
        initializePool(Config.getHostRedis(), Config.getPortRedis());
    }

    private void initializePool(String host, int port) {
        try {
            Core.info("Redis - trying to connect to " + host + ":" + port);

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxIdle(10);
            jedisPool = new JedisPool(poolConfig, host, port);

            // Test the connection
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }

            Core.info("Redis - connection established successfully");
        } catch (JedisConnectionException e) {
            Core.severe("Redis - no connection established");
            jedisPool = null;
        } catch (JedisAccessControlException e) {
            Core.severe("Redis - access denied (invalid credentials)");
            jedisPool = null;
        } catch (Exception e) {
            Core.severe("Redis - unexpected error during connection setup", e);
            jedisPool = null;
        }
    }

    private Optional<Jedis> getJedis() {
        if (jedisPool == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(jedisPool.getResource());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String get(String key) {
        return getJedis()
                .map(j -> {
                    try (j) {
                        return j.get(key);
                    }
                })
                .orElse(localCache.get(key));
    }

    public void set(String key, String value) {
        getJedis().ifPresentOrElse(
                j -> {
                    try (j) {
                        j.set(key, value);
                    }
                },
                () -> localCache.put(key, value)
        );
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
