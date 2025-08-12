package de.einfachesache.proxymanager.core;

import de.einfachesache.api.util.FileUtils;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.DiscordServerProfile;

import java.io.InputStream;
import java.util.*;

@SuppressWarnings("unused")
public class Config {

    private static String serverName;

    private static Map<String, Integer> countingNumbers;
    private static Map<String, Long> giveawayEndtimes;
    private static Map<String, Set<String>> giveawayParticipantSets;
    private static Map<String, Set<String>> eligibleUsersForGiveawaySets;

    private static int portRedis;
    private static String hostRedis;
    private static String userRedis;
    private static String passwordRedis;

    private static boolean discordEnable;
    private static String activity;
    private static String activityType;
    private static Set<String> guildIDs;
    private static HashMap<String, DiscordServerProfile> discordServerProfiles;
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
        countingNumbers = new HashMap<>();
        giveawayEndtimes = new HashMap<>();
        giveawayParticipantSets = new HashMap<>();
        eligibleUsersForGiveawaySets = new HashMap<>();

        guildIDs.forEach(guildID -> {
            countingNumbers.put(guildID, data.getInt("servers." + guildID + ".counting.current-number"));
            giveawayEndtimes.put(guildID, data.getLong("servers." + guildID + ".giveaway.endTime", -1));
            giveawayParticipantSets.put(guildID, new HashSet<>(data.getStringList("servers." + guildID + ".giveaway.participants")));
            eligibleUsersForGiveawaySets.put(guildID, new HashSet<>(data.getStringList("servers." + guildID + ".giveaway.eligible-users")));
        });
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
        guildIDs = new HashSet<>();
        discordServerProfiles = new HashMap<>();

        discordEnable = discordModule.getBoolean("discord.enabled", false);
        activityType = discordModule.get("discord.activity-type", "");
        activity = discordModule.get("discord.activity", "");
        connectTCPServer = discordModule.getBoolean("discord.tcp-server.connect", false);
        portTCPServer = discordModule.getInt("discord.tcp-server.port", 6666);

        guildIDs = discordModule.getConfigurationSection("servers").getKeys(false);
        guildIDs.forEach(guildId -> discordServerProfiles.put(guildId, new DiscordServerProfile(
                // guildId
                guildId,
                // name
                discordModule.get("servers." + guildId + ".name", "NULL"),
                // joinRoleId (user-role-id im YAML)
                String.valueOf(discordModule.getLong("servers." + guildId + ".join-role-id", -1)),
                // staffRoleId
                String.valueOf(discordModule.getLong("servers." + guildId + ".staff-role-id", -1)),
                // betaTesterRoleId
                String.valueOf(discordModule.getLong("servers." + guildId + ".beta-tester-role-id", -1)),

                // logChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".log-channel-id", -1)),
                // ticketCategoryId
                String.valueOf(discordModule.getLong("servers." + guildId + ".tickets-category-id", -1)),
                // countingChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".counting-channel-id", -1)),
                // giveawayChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".giveaway-channel-id", -1)),
                // inviteLogChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".invite-log-channel-id", -1))
        )));
    }

    public static Integer getCountingNumber(String guildID) {
        return countingNumbers.get(guildID);
    }

    public static long getGiveawayEndtimeInMilli(String guildID) {
        return giveawayEndtimes.get(guildID);
    }

    public static Set<String> getGiveawayParticipantSet(String guildID) {
        return giveawayParticipantSets.get(guildID);
    }

    public static Set<String> getEligibleUsersForGiveawaySet(String guildID) {
        return eligibleUsersForGiveawaySets.get(guildID);
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

    public static Set<String> getGuildIDs() {
        return guildIDs;
    }

    public static DiscordServerProfile getDiscordServerProfile(String guildID) {
        return discordServerProfiles.getOrDefault(guildID, new DiscordServerProfile());
    }

    public static String getJoinRoleID(String guildID) {
        return discordServerProfiles.get(guildID).getJoinRoleId();
    }

    public static String getBetaTesterRoleID(String guildID) {
        return discordServerProfiles.get(guildID).getBetaTesterRoleId();
    }

    public static String getTicketsCategoryID(String guildID) {
        return discordServerProfiles.get(guildID).getTicketCategoryId();
    }

    public static String getStaffRoleID(String guildID) {
        return discordServerProfiles.get(guildID).getStaffRoleId();
    }

    public static String getLogChannelID(String guildID) {
        return discordServerProfiles.get(guildID).getLogChannelId();
    }

    public static String getCountingChannelID(String guildID) {
        return discordServerProfiles.get(guildID).getCountingChannelId();
    }

    public static String getGiveawayChannelID(String guildID) {
        return discordServerProfiles.get(guildID).getGiveawayChannelId();
    }

    public static String getInviteLogChannelID(String guildID) {
        return discordServerProfiles.get(guildID).getInviteLogChannelId();
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


    public static void setCountingNumber(String guildId, Integer countingNumber) {
        countingNumbers.put(guildId, countingNumber);
        save(data, "servers." + guildId + ".counting.current-number", countingNumber);
    }

    public static void setGiveawayEndtime(String guildId, long endTime) {
        giveawayEndtimes.put(guildId, endTime);
        save(data, "servers." + guildId + ".giveaway.endTime", endTime);
    }

    public static boolean addGiveawayParticipant(String guildId, String participant) {
        Set<String> set = giveawayParticipantSets.computeIfAbsent(guildId, id -> new HashSet<>());
        boolean added = set.add(participant);
        save(data, "servers." + guildId + ".giveaway.participants", new ArrayList<>(set));
        return added;
    }

    public static void addEligibleUserForGiveaway(String guildId, String userId) {
        var set = eligibleUsersForGiveawaySets.computeIfAbsent(guildId, id -> new HashSet<>());
        set.add(userId);
        save(data, "servers." + guildId + ".giveaway.eligible-users", new ArrayList<>(set));
    }

    public static void resetLastGiveaway(String guildId) {
        // Ende-Zeit zurücksetzen
        setGiveawayEndtime(guildId, -1L);

        // Teilnehmer- und Eligibility-Sets für diese Guild leeren
        giveawayParticipantSets
                .computeIfAbsent(guildId, id -> new HashSet<>())
                .clear();
        eligibleUsersForGiveawaySets
                .computeIfAbsent(guildId, id -> new HashSet<>())
                .clear();

        // kompletten Giveaway-Abschnitt in der Config für diese Guild löschen
        save(data, "servers." + guildId + ".giveaway", null);
    }


    public static void setTicketsCategoryID(String guildID, String ticketCategoryID) {
        discordServerProfiles.get(guildID).setTicketCategoryId(ticketCategoryID);
        save(discordModule, "servers." + guildID + ".tickets-category-id", ticketCategoryID);
    }

    public static void setStaffRoleID(String guildID, String staffRoleID) {
        discordServerProfiles.get(guildID).setStaffRoleId(staffRoleID);
        save(discordModule, "servers." + guildID + ".staff-role-id", staffRoleID);
    }

    public static void setLogChannelID(String guildID, String logChannelID) {
        discordServerProfiles.get(guildID).setLogChannelId(logChannelID);
        save(discordModule, "servers." + guildID + ".log-channel-id", logChannelID);
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

