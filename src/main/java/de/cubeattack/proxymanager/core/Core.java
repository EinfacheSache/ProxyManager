package de.cubeattack.proxymanager.core;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.shutdown.ShutdownHook;
import de.cubeattack.api.util.FileUtils;
import de.cubeattack.proxymanager.discord.DiscordAPI;
import org.slf4j.LoggerFactory;

public class Core {

    private static boolean isMinecraftServer;

    public static FileUtils minecraftModule;
    public static FileUtils discordModule;
    public static FileUtils redisModule;
    public static FileUtils mysqlModule;
    public static FileUtils config;

    private static RedisConnector RedisConnector;
    private static DiscordAPI discordAPI;
    private static DataSourceProvider datasource;

    public static void main(String[] args) {
        run(false, LoggerFactory.getLogger("de.cubeattack"));
    }

    public static void run(boolean isMinecraftServer, Object logger) {
        Core.isMinecraftServer = isMinecraftServer;
        LogManager.getLogger().setLogger(logger);
        run();
    }

    public static void run() {
        Thread.currentThread().setName("NETWORK");

        ShutdownHook.register(Core::shutdown);

        //Core.info("running ProxyManager on version " + VersionUtils.getPomVersion(VersionUtils.class) + " build " + VersionUtils.getBuild());

        minecraftModule = new FileUtils(Core.class.getResourceAsStream("/modules/minecraft.yml"), isMinecraftServer ? "plugins/ProxyManager" : "./", "modules/minecraft.yml");
        discordModule = new FileUtils(Core.class.getResourceAsStream("/modules/discord.yml"), isMinecraftServer ? "plugins/ProxyManager" : "./", "modules/discord.yml");
        redisModule = new FileUtils(Core.class.getResourceAsStream("/modules/redis.yml"), isMinecraftServer ? "plugins/ProxyManager" : "./", "modules/redis.yml");
        mysqlModule = new FileUtils(Core.class.getResourceAsStream("/modules/mysql.yml"), isMinecraftServer ? "plugins/ProxyManager" : "./", "modules/mysql.yml");
        config = new FileUtils(Core.class.getResourceAsStream("/config.yml"), isMinecraftServer ? "plugins/ProxyManager" : "./", "config.yml");

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

    public static void severe(String output, Throwable err) {
        LogManager.getLogger().error(output, err);
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
