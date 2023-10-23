package de.cubeattack.proxymanager.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisAccessControlException;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisConnector {

    private Jedis jedis = null;

    public RedisConnector() {

        if (!Config.connectRedis()) return;

        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxIdle(1);
            JedisPool jedisPool = new JedisPool(poolConfig, Config.getHostRedis(), Config.getPortRedis());

            Core.info("Redis - Try to connect to " + Config.getHostRedis() + ":" + Config.getPortRedis());

            jedis = jedisPool.getResource();
            jedis.set(Config.getUserRedis(), Config.getPasswdRedis());
            jedis.connect();

            Core.info("Redis - Connection successful");
        } catch (JedisConnectionException | JedisAccessControlException ex) {
            Core.severe("Redis - " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Jedis getJedis() {
        return jedis;
    }
}
