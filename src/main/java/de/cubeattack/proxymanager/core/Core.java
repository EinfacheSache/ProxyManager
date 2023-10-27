package de.cubeattack.proxymanager.core;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.shutdown.ShutdownHook;
import de.cubeattack.api.util.FileUtils;
import de.cubeattack.proxymanager.discord.DiscordAPI;

import java.util.logging.Logger;

public class Core {

    public static FileUtils discordModule;
    public static FileUtils redisModule;
    public static FileUtils mysqlModule;
    public static FileUtils config;

    private static RedisConnector RedisConnector;
    private static DiscordAPI discordAPI;
    private static DataSourceProvider datasource;

    public static void main(String[] args) {
        run();
    }

    public static void run(Logger logger) {
        LogManager.getLogger().setLogger(logger);
        run();
    }

    public static void run() {
        Thread.currentThread().setName("NETWORK");

        ShutdownHook.register(Core::shutdown);

        //Core.info("running ProxyManager on version " + VersionUtils.getPomVersion(VersionUtils.class) + " build " + VersionUtils.getBuild());

        discordModule = new FileUtils(Core.class.getResourceAsStream("/modules/discord.yml"), "plugins/ProxyManager", "modules/discord.yml");
        redisModule = new FileUtils(Core.class.getResourceAsStream("/modules/redis.yml"), "plugins/ProxyManager", "modules/redis.yml");
        mysqlModule = new FileUtils(Core.class.getResourceAsStream("/modules/mysql.yml"), "plugins/ProxyManager", "modules/mysql.yml");
        config = new FileUtils(Core.class.getResourceAsStream("/config.yml"), "plugins/ProxyManager", "config.yml");

        Config.loadModules();

        RedisConnector = new RedisConnector();
        datasource = new DataSourceProvider();
        discordAPI = new DiscordAPI();
    }

    public static void shutdown() {
        info("Bots stopping ...");
        RedisConnector.close();
        discordAPI.shutdown();
        TcpServer.stop();
        info("Bots successfully stopped");
    }

    public static void debug(String output) {
        LogManager.getLogger().debug(output);
    }

    public static void info(String output) {
        LogManager.getLogger().info(output);
    }

    public static void warn(String output) {
        LogManager.getLogger().warn(output);
    }

    public static void severe(String output) {
        LogManager.getLogger().error(output);
    }

    public static RedisConnector getRedisConnector() {
        return RedisConnector;
    }

    public static DiscordAPI getDiscordAPI() {
        return discordAPI;
    }

    public static DataSourceProvider getDatasource() {
        return datasource;
    }
}
