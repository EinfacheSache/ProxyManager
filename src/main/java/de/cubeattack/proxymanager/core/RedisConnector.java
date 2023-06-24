package de.cubeattack.proxymanager.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisConnector {

    private final Jedis jedis = new Jedis(Config.getHostRedis(), Config.getPortRedis());

    public RedisConnector() {

        if (!Config.connectRedis()) return;

        try {
            Core.info("Redis - Try to connect to " + Config.getHostRedis() + ":" + Config.getPortRedis());
            jedis.connect();
            Core.info("Redis - Connections successful");
        } catch (JedisConnectionException ex) {
            Core.severe("Redis - " + ex.getMessage());
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Jedis getJedis() {
        return jedis;
    }
}
