package de.einfachesache.proxymanager.core;


import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.api.logger.LogManager;
import de.einfachesache.api.shutdown.ShutdownHook;
import de.einfachesache.api.util.FileUtils;
import de.einfachesache.api.util.version.VersionUtils;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.velocity.ProxyInstance;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Core {

    public static final UUID DEV_UUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");

    private static ProxyInstance proxyInstance;
    private static Logger logger;

    public static FileUtils minecraftModule;
    public static FileUtils discordModule;
    public static FileUtils redisModule;
    public static FileUtils mysqlModule;
    public static FileUtils config;
    public static FileUtils data;

    private static DataSourceProvider datasource;
    private static RedisConnector redisConnector;
    private static DiscordAPI discordAPI;

    public static Long UPTIME = 0L;

    public static void main(String[] args) {
        run(null, LoggerFactory.getLogger(Core.class));
    }

    public static void run(ProxyInstance proxyInstance, Logger logger) {

        LogManager.getLogger().setLogger(logger);

        Core.logger = logger;
        Core.proxyInstance = proxyInstance;
        Core.UPTIME = System.currentTimeMillis();
        Core.discordAPI = new DiscordAPI();
        Core.redisConnector = new RedisConnector();
        Core.datasource = new DataSourceProvider();

        run();
    }

    public static void run() {
        Thread.currentThread().setName("PROXY-MANAGER");

        ShutdownHook.register(Core::shutdown);

        Core.info("running ProxyManager on version " + VersionUtils.getPomVersion(Core.class));

        minecraftModule = new FileUtils(Core.class.getResource("/modules/minecraft.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/minecraft.yml");
        discordModule = new FileUtils(Core.class.getResource("/modules/discord.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/discord.yml");
        redisModule = new FileUtils(Core.class.getResource("/modules/redis.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/redis.yml");
        mysqlModule = new FileUtils(Core.class.getResource("/modules/mysql.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "modules/mysql.yml");
        config = new FileUtils(Core.class.getResource("/config.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "config.yml");
        data = new FileUtils(Core.class.getResource("/data.yml"), isMinecraftServer() ? "plugins/ProxyManager" : ".", "data.yml");

        Config.loadModules();

        if (isMinecraftServer()) {
            Configurator.setLevel(logger.getName(), Level.toLevel(Config.getLogLevel(), Level.INFO));
        }

        initAsync(proxyInstance);
    }

    public static void initAsync(ProxyInstance proxyInstance) {
        AsyncExecutor.getService().submit(() -> {
            discordAPI.init(proxyInstance);
            redisConnector.init();
            datasource.init();
        });

        if (isMinecraftServer()) {
            return;
        }

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            severe(e.getMessage(), e);
        }
    }


    public static void shutdown() {
        info("Stopping running services...");
        TcpServer.stop();
        discordAPI.shutdown();
        redisConnector.close();
        info("Services successfully stopped");
    }

    public static void trace(String output) {
        LogManager.getLogger().trace(output);
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
        return redisConnector;
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
