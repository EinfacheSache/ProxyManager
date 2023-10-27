package de.cubeattack.proxymanager.core;

import de.cubeattack.api.util.FileUtils;

import java.util.List;

@SuppressWarnings("unused")
public class Config {

    private static int portRedis;
    private static boolean connectRedis;
    private static String hostRedis;
    private static String userRedis;
    private static String passwordRedis;

    private static boolean discordEnable;
    private static String token;
    private static String activity;
    private static String activityType;
    private static String guildID;
    private static String categoryID;
    private static String teamRoleID;
    private static String logChannelID;
    private static int portTCPServer;
    private static boolean connectTCPServer;

    private static int portMySQL;
    private static boolean connectMySQl;
    private static String hostMySQL;
    private static String user;
    private static String password;
    private static String database;

    private static boolean manageConnectionEnabled;
    private static boolean playerHeadAsServerIcon;
    private static boolean maintenanceMode;
    private static String serverDomainName;
    private static String verifyServerDomain;
    private static String verifyServer;
    private static List<String> allowedDomains;


    public static void loadModules() {
        loadDiscordModule();
        loadConfigModule();
        loadMySQLModule();
        loadRedisModule();
    }

    private static final FileUtils redis = Core.redisModule;

    private static void loadRedisModule() {
        connectRedis = redis.getBoolean("redis.connect", true);
        portRedis = redis.getInt("redis.port", 6379);
        hostRedis = redis.getString("redis.host", "127.0.0.1");
        userRedis = redis.getString("redis.user", "default");
        passwordRedis = redis.getString("redis.password", "");
    }

    private static final FileUtils mysql = Core.mysqlModule;

    private static void loadMySQLModule() {
        connectMySQl = mysql.getBoolean("mysql.connect", false);
        portMySQL = mysql.getInt("mysql.port", 3306);
        hostMySQL = mysql.getString("mysql.host", "127.0.0.1");
        user = mysql.getString("mysql.user", "root");
        password = mysql.getString("mysql.password", "");
        database = mysql.getString("mysql.database", "");
    }

    private static final FileUtils config = Core.config;

    private static void loadConfigModule() {
        maintenanceMode = config.getBoolean("maintenance-mode", false);
        manageConnectionEnabled = config.getBoolean("manage-connections.enabled", false);
        playerHeadAsServerIcon = config.getBoolean("manage-connections.player-head-as-server-icon", false);
        serverDomainName = config.getString("server-domain-name", "yourdomain.com");
        verifyServerDomain = config.getString("manage-connections.verify-server-domain", "verify.yourdomain.com");
        verifyServer = config.getString("manage-connections.verify-server", "Verify");
        allowedDomains = config.getListAsList("manage-connections.allowed-domains");
    }

    private static final FileUtils discordModule = Core.discordModule;

    private static void loadDiscordModule() {
        discordEnable = discordModule.getBoolean("discord.enabled", false);
        token = discordModule.getString("discord.token", "");
        guildID = discordModule.getString("discord.guild-id", "");
        activityType = discordModule.getString("discord.activity-type", "");
        activity = discordModule.getString("discord.activity", "");
        categoryID = discordModule.getString("discord.tickets.category-id", "");
        teamRoleID = discordModule.getString("discord.tickets.team-role-id", "");
        logChannelID = discordModule.getString("discord.tickets.log-channel-id", "");
        connectTCPServer = discordModule.getBoolean("discord.tcp-server.connect", false);
        portTCPServer = discordModule.getInt("discord.tcp-server.port", 6666);
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

    public static String getPasswordRedis() {
        return passwordRedis;
    }


    public static boolean isDiscordDisabled() {
        return !discordEnable;
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

    public static String getCategoryID() {
        return categoryID;
    }

    public static String getTeamRoleID() {
        return teamRoleID;
    }

    public static String getLogChannelID() {
        return logChannelID;
    }

    public static int getPortTCPServer() {
        return portTCPServer;
    }

    public static boolean isConnectTCPServer() {
        return connectTCPServer;
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
        return manageConnectionEnabled;
    }

    public static boolean isPlayerHeadAsServerIcon() {
        return playerHeadAsServerIcon;
    }

    public static boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public static String getServerDomainName() {
        return serverDomainName;
    }

    public static String getVerifyServerDomain() {
        return verifyServerDomain;
    }

    public static String getVerifyServer() {
        return verifyServer;
    }

    public static List<String> getAllowedDomains() {
        return allowedDomains;
    }

    public static void setCategoryID(String categoryID) {
        Config.categoryID = categoryID;
        save(discordModule, "discord.tickets.category-id", categoryID);
    }

    public static void setTeamRoleID(String teamRoleID) {
        Config.teamRoleID = teamRoleID;
        save(discordModule, "discord.tickets.team-role-id", teamRoleID);
    }

    public static void setLogChannelID(String logChannelID) {
        Config.logChannelID = logChannelID;
        save(discordModule, "discord.tickets.log-channel-id", logChannelID);
    }

    public static void setMaintenanceMode(boolean maintenanceMode) {
        Config.maintenanceMode = maintenanceMode;
        save(config, "MaintenanceMode", maintenanceMode);
    }

    public static void save(FileUtils file, String key, Object value) {
        file.set(key, value);
        file.save();
    }
}

