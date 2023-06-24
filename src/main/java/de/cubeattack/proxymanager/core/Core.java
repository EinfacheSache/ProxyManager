package de.cubeattack.proxymanager.core;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.shutdown.ShutdownHook;
import de.cubeattack.api.utils.FileUtils;
import de.cubeattack.api.utils.VersionUtils;
import de.cubeattack.proxymanager.discord.DiscordAPI;
import org.slf4j.Logger;

public class Core{

    public static FileUtils tcpServerModule;
    public static FileUtils discordModule;
    public static FileUtils redisModule;
    public static FileUtils mysqlModule;
    public static FileUtils config;

    private static RedisConnector RedisConnector;
    private static DiscordAPI discordAPI;
    private static DataSourceProvider datasource;
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        Thread.currentThread().setName("NETWORK");

        ShutdownHook.register(Core::shutdown);

        Core.info("running ProxyManager on version " + VersionUtils.getVersion() + " build " +  VersionUtils.getBuild());

        tcpServerModule = new FileUtils(Core.class.getResourceAsStream("/modules/tpcServer.yml"), "plugins/ProxyManager","modules/tcpServer.yml");
        discordModule = new FileUtils(Core.class.getResourceAsStream("/modules/discord.yml"), "plugins/ProxyManager","modules/discord.yml");
        redisModule = new FileUtils(Core.class.getResourceAsStream("/modules/redis.yml"), "plugins/ProxyManager","modules/redis.yml");
        mysqlModule = new FileUtils(Core.class.getResourceAsStream("/modules/mysql.yml"),"plugins/ProxyManager", "modules/mysql.yml");
        config = new FileUtils(Core.class.getResourceAsStream("/config.yml"),"plugins/ProxyManager", "config.yml");

        Config.loadModules();

        RedisConnector = new RedisConnector();
        datasource = new DataSourceProvider();
        discordAPI = new DiscordAPI();
    }

    public static void shutdown(){
        info("Bots stopping ...");
        discordAPI.shutdown();
        TcpServer.stop();
        info("Bots successfully stopped");
    }
    public static void debug(String output){
        logger.debug(output);
    }
    public static void info(String output){
        logger.info(output);
    }
    public static void warn(String output){
        logger.warn(output);
    }
    public static void severe(String output){
        logger.error(output);
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
