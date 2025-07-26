package de.einfachesache.proxymanager.core;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.shutdown.ShutdownHook;
import de.cubeattack.api.util.FileUtils;
import de.cubeattack.api.util.versioning.VersionUtils;
import de.einfachesache.proxymanager.ProxyInstance;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Core {

    public static final UUID ALLOWED_UUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");

    private static ProxyInstance proxyInstance;

    public static FileUtils minecraftModule;
    public static FileUtils discordModule;
    public static FileUtils redisModule;
    public static FileUtils mysqlModule;
    public static FileUtils config;
    public static FileUtils data;

    private static RedisConnector RedisConnector;
    private static DiscordAPI discordAPI;
    private static DataSourceProvider datasource;

    public static Long UPTIME = 0L;

    public static void main(String[] args) {
        run(null, LoggerFactory.getLogger(Core.class));
    }

    public static void run(ProxyInstance proxyInstance, Object logger) {
        Core.proxyInstance = proxyInstance;
        LogManager.getLogger().setLogger(logger);
        run();
    }

    public static void run() {
        Thread.currentThread().setName("PROXY-MANAGER");

        ShutdownHook.register(Core::shutdown);

        Core.info("running ProxyManager on version " + VersionUtils.getPomVersion(Core.class));

        minecraftModule = new FileUtils(Core.class.getResourceAsStream("/modules/minecraft.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/minecraft.yml");
        discordModule = new FileUtils(Core.class.getResourceAsStream("/modules/discord.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/discord.yml");
        redisModule = new FileUtils(Core.class.getResourceAsStream("/modules/redis.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/redis.yml");
        mysqlModule = new FileUtils(Core.class.getResourceAsStream("/modules/mysql.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/mysql.yml");
        config = new FileUtils(Core.class.getResourceAsStream("/config.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "config.yml");
        data = new FileUtils(Core.class.getResourceAsStream("/data.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "data.yml");

        Config.loadModules();

        RedisConnector = new RedisConnector();
        datasource = new DataSourceProvider();
        discordAPI = new DiscordAPI(proxyInstance);
    }

    public static void shutdown() {
        info("Stopping running services...");
        RedisConnector.close();
        discordAPI.shutdown();
        TcpServer.stop();
        info("Services successfully stopped");
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

    public static boolean isMinecraftServer() {
        return proxyInstance != null;
    }
}
