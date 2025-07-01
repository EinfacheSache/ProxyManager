package de.cubeattack.proxymanager.core;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisAccessControlException;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisConnector {

    private JedisPool jedisPool = null;

    public RedisConnector() {

        if (!Config.connectRedis()) return;
        if (!Core.isMinecraftServer()) return;

        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxIdle(10);
            jedisPool = new JedisPool(poolConfig, Config.getHostRedis(), Config.getPortRedis());

            Core.info("Redis - Try to connect to " + Config.getHostRedis() + ":" + Config.getPortRedis());

            Core.info("Redis - Connection successful");
        } catch (JedisConnectionException | JedisAccessControlException ex) {
            Core.severe("Redis - " + ex.getMessage());
        } catch (Exception ex) {
            Core.severe("Redis - ", ex);
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
