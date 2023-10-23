package de.cubeattack.proxymanager.core;

import de.cubeattack.api.util.FileUtils;

import java.util.List;

@SuppressWarnings("unused")
public class Config {


    private static int portTCPServer;
    private static boolean connectTCPServer;

    private static int portRedis;
    private static boolean connectRedis;
    private static String hostRedis;
    private static String userRedis;
    private static String passwdRedis;

    private static boolean discordEnable;
    private static String token;
    private static String activity;
    private static String activityType;
    private static String guildID;

    private static int portMySQL;
    private static boolean connectMySQl;
    private static String hostMySQL;
    private static String user;
    private static String password;
    private static String database;

    private static boolean ManageConnectionEnabled;
    private static boolean PlayerHeadAsServerIcon;
    private static String ServerDomainName;
    private static String VerifyServerDomain;
    private static String VerifyServer;
    private static List<String> AllowedDomains;


    public static void loadModules() {
        loadTCPServerModule();
        loadDiscordModule();
        loadConfigModule();
        loadMySQLModule();
        loadRedisModule();
    }

    private static void loadTCPServerModule() {
        FileUtils tpcServer = Core.tcpServerModule;
        connectTCPServer = tpcServer.getBoolean("tcpServer.connect", false);
        portTCPServer = tpcServer.getInt("tcpServer.port", 6666);
    }

    private static void loadRedisModule() {
        FileUtils redis = Core.redisModule;
        connectRedis = redis.getBoolean("redis.connect", true);
        portRedis = redis.getInt("redis.port", 6379);
        hostRedis = redis.getString("redis.host", "127.0.0.1");
        userRedis = redis.getString("redis.user", "default");
        passwdRedis = redis.getString("redis.passwd", "");
    }

    private static void loadDiscordModule() {
        FileUtils discordModule = Core.discordModule;
        discordEnable = discordModule.getBoolean("discord.enable", false);
        token = discordModule.getString("discord.Token", "");
        guildID = discordModule.getString("discord.GuildID", "");
        activityType = discordModule.getString("discord.ActivityType", "");
        activity = discordModule.getString("discord.Activity", "");
    }

    private static void loadMySQLModule() {
        FileUtils mysql = Core.mysqlModule;
        connectMySQl = mysql.getBoolean("mysql.connect", false);
        portMySQL = mysql.getInt("mysql.port", 3306);
        hostMySQL = mysql.getString("mysql.host", "127.0.0.1");
        user = mysql.getString("mysql.user", "root");
        password = mysql.getString("mysql.password", "");
        database = mysql.getString("mysql.database", "");
    }
    private static void loadConfigModule() {
        FileUtils config = Core.config;
        ManageConnectionEnabled = config.getBoolean("ManageConnection.enabled", false);
        PlayerHeadAsServerIcon = config.getBoolean("ManageConnection.PlayerHeadAsServerIcon", false);
        ServerDomainName = config.getString("ServerDomainName", "yourdomain.com");
        VerifyServerDomain = config.getString("ManageConnection.VerifyServerDomain", "verify.yourdomain.com");
        VerifyServer = config.getString("ManageConnection.VerifyServer", "Verify");
        AllowedDomains = config.getListAsList("ManageConnection.AllowedDomains");
    }


    public static int getPortTCPServer() {
        return portTCPServer;
    }

    public static boolean isConnectTCPServer() {
        return connectTCPServer;
    }


    public static int getPortRedis() {
        return portRedis;
    }

    public static boolean connectRedis() {
        return connectRedis;
    }

    public static String getHostRedis() {
        return hostRedis;
    }

    public static String getUserRedis() {
        return userRedis;
    }

    public static String getPasswdRedis() {
        return passwdRedis;
    }


    public static String getToken() {
        return token;
    }

    public static String getActivity() {
        return activity;
    }

    public static String getActivityType() {
        return activityType;
    }

    public static String getGuildID() {
        return guildID;
    }

    public static boolean isDiscordDisabled() {
        return !discordEnable;
    }


    public static int getMySQLPort() {
        return portMySQL;
    }

    public static boolean connectMySQL() {
        return connectMySQl;
    }

    public static String getMySQLHost() {
        return hostMySQL;
    }

    public static String getMySQLUser() {
        return user;
    }

    public static String getMySQLPassword() {
        return password;
    }

    public static String getMySQLDatabase() {
        return database;
    }


    public static boolean isManageConnectionEnabled() {
        return ManageConnectionEnabled;
    }

    public static boolean isPlayerHeadAsServerIcon() {
        return PlayerHeadAsServerIcon;
    }

    public static String getServerDomainName() {
        return ServerDomainName;
    }

    public static String getVerifyServerDomain() {
        return VerifyServerDomain;
    }

    public static String getVerifyServer() {
        return VerifyServer;
    }

    public static List<String> getAllowedDomains() {
        return AllowedDomains;
    }

}

