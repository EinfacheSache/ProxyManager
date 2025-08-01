package de.einfachesache.proxymanager.core;

import de.cubeattack.api.util.FileUtils;
import de.einfachesache.proxymanager.discord.DiscordAPI;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@SuppressWarnings("unused")
public class Config {

    private static String serverName;

    private static Integer countingNumber;
    private static Long giveawayEndtime;
    private static Set<String> giveawayParticipantSet;
    private static Set<String> eligibleUsersForGiveawaySet;

    private static int portRedis;
    private static String hostRedis;
    private static String userRedis;
    private static String passwordRedis;

    private static boolean discordEnable;
    private static String activity;
    private static String activityType;
    private static String guildID;
    private static String categoryID;
    private static String teamRoleID;
    private static String logChannelID;
    private static String countingChannelID;
    private static String giveawayChannelID;
    private static String betaTesterRoleID;
    private static String userRoleID;
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
        loadMinecraftModule();
        loadDiscordModule();
        loadMySQLModule();
        loadRedisModule();
        loadConfig();
        loadData();
    }

    private static final FileUtils data = Core.data;

    private static void loadData() {
        countingNumber = data.getInt("discord.counting.current-number");
        giveawayEndtime = data.getLong("discord.giveaway.endTime", -1);
        giveawayParticipantSet = new HashSet<>(data.getStringList("discord.giveaway.participants"));
        eligibleUsersForGiveawaySet = new HashSet<>(data.getStringList("discord.giveaway.eligible-users"));
    }

    private static final FileUtils config = Core.config;

    private static void loadConfig() {
        serverName = config.get("server-name", "YourServerName");
    }

    private static final FileUtils redis = Core.redisModule;

    private static void loadRedisModule() {
        portRedis = redis.getInt("redis.port", 1337);
        hostRedis = redis.get("redis.host", "127.0.0.1").replace("localhost", "127.0.0.1");
        userRedis = redis.get("redis.user", "default");
        passwordRedis = redis.get("redis.password", "");
    }

    private static final FileUtils mysql = Core.mysqlModule;

    private static void loadMySQLModule() {
        connectMySQl = mysql.getBoolean("mysql.connect", false);
        portMySQL = mysql.getInt("mysql.port", 3306);
        hostMySQL = mysql.get("mysql.host", "127.0.0.1");
        user = mysql.get("mysql.user", "root");
        password = mysql.get("mysql.password", "");
        database = mysql.get("mysql.database", "");
    }


    private static final FileUtils minecraftModule = Core.minecraftModule;

    private static void loadMinecraftModule() {
        maintenanceMode = minecraftModule.getBoolean("maintenance-mode", false);
        manageConnectionEnabled = minecraftModule.getBoolean("manage-connections.enabled", false);
        playerHeadAsServerIcon = minecraftModule.getBoolean("manage-connections.player-head-as-server-icon", false);
        serverDomainName = minecraftModule.get("server-domain-name", "yourdomain.com");
        verifyServerDomain = minecraftModule.get("manage-connections.verify-server-domain", "verify.yourdomain.com");
        verifyServer = minecraftModule.get("manage-connections.verify-server", "Verify");
        allowedDomains = minecraftModule.getStringList("manage-connections.allowed-domains");
    }


    private static final FileUtils discordModule = Core.discordModule;

    private static void loadDiscordModule() {
        discordEnable = discordModule.getBoolean("discord.enabled", false);
        guildID = discordModule.get("discord.guild-id", "");
        activityType = discordModule.get("discord.activity-type", "");
        activity = discordModule.get("discord.activity", "");
        userRoleID = discordModule.get("discord.user-role-id", "");
        betaTesterRoleID = discordModule.get("discord.beta-tester-role-id", "");
        categoryID = discordModule.get("discord.tickets.category-id", "");
        teamRoleID = discordModule.get("discord.tickets.team-role-id", "");
        logChannelID = discordModule.get("discord.tickets.log-channel-id", "");
        countingChannelID = discordModule.get("discord.counting-channel-id", "");
        giveawayChannelID = discordModule.get("discord.giveaway-channel-id", "");
        connectTCPServer = discordModule.getBoolean("discord.tcp-server.connect", false);
        portTCPServer = discordModule.getInt("discord.tcp-server.port", 6666);
    }

    public static Integer getCountingNumber() {
        return countingNumber;
    }

    public static long getGiveawayEndtimeInMilli() {
        return giveawayEndtime;
    }

    public static Set<String> getGiveawayParticipantSet() {
        return giveawayParticipantSet;
    }

    public static Set<String> getEligibleUsersForGiveawaySet() {
        return eligibleUsersForGiveawaySet;
    }


    public static String getServerName() {
        return serverName;
    }

    public static int getPortRedis() {
        return portRedis;
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
        try {
            Properties prop = new Properties();
            InputStream in = DiscordAPI.class.getResourceAsStream("/application.properties");
            prop.load(in);
            return prop.getProperty("TOKEN");
        } catch (Exception ex) {
            Core.severe("./application.properties file can't be found", ex);
            return "NOT_FOUND";
        }
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

    public static String getUserRoleID() {
        return userRoleID;
    }

    public static String getBetaTesterRoleID() {
        return betaTesterRoleID;
    }

    public static String getTicketsCategoryID() {
        return categoryID;
    }

    public static String getStaffRoleID() {
        return teamRoleID;
    }

    public static String getLogChannelID() {
        return logChannelID;
    }

    public static String getCountingChannelID() {
        return countingChannelID;
    }

    public static String getGiveawayChannelID() {
        return giveawayChannelID;
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


    public static void setCountingNumber(Integer countingNumber) {
        Config.countingNumber = countingNumber;
        save(data, "discord.counting.current-number", countingNumber);
    }


    public static void setGiveawayEndtime(long entTime) {
        Config.giveawayEndtime = entTime;
        save(data, "discord.giveaway.endTime", entTime);
    }

    public static boolean addGiveawayParticipant(String participant) {
        boolean added = Config.giveawayParticipantSet.add(participant);
        save(data, "discord.giveaway.participants", Config.giveawayParticipantSet.stream().toList());
        return added;
    }

    public static void addEligibleUsersForGiveaway(String eligibleUsers) {
        Config.eligibleUsersForGiveawaySet.add(eligibleUsers);
        save(data, "discord.giveaway.eligible-users", Config.eligibleUsersForGiveawaySet.stream().toList());
    }

    public static void resetLastGiveaway() {
        setGiveawayEndtime(-1L);
        Config.giveawayParticipantSet.clear();
        Config.eligibleUsersForGiveawaySet.clear();
        save(data, "discord.giveaway", null);
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
        save(minecraftModule, "maintenance-mode", maintenanceMode);
    }


    public static void save(FileUtils file, String key, Object value) {
        file.set(key, value);
        file.save();
    }
}

