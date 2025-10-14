package de.einfachesache.proxymanager.core;

import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.api.util.FileUtils;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.DiscordServerProfile;
import de.einfachesache.proxymanager.velocity.VPermissionProvider;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class Config {

    private static String logLevel;
    private static String serverName;

    private static Map<String, String> whitelistedPlayers;
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
    private static boolean connectTCPServer;

    private static int portMySQL;
    private static boolean connectMySQl;
    private static String hostMySQL;
    private static String user;
    private static String password;
    private static String database;

    private static boolean playerHeadAsServerIcon;
    private static boolean maintenanceMode;
    private static boolean eventWhitelist;
    private static boolean customMotd;
    private static boolean hostAllowlist;
    private static boolean proxyProtocol;
    private static String serverDomainName;
    private static String verifyServerDomain;
    private static String verifyServer;
    private static String pingVersionName;
    private static String assignedGuildID;
    private static List<String> maintenanceAccess;
    private static List<String> allowedDomains;
    private static List<String> allowedSubnet;

    public static void loadModules() {
        loadMinecraftModule();
        loadDiscordModule();
        loadMySQLModule();
        loadRedisModule();
        loadConfig();
        loadData();
    }

    public static CompletableFuture<Boolean> reloadFilesAsync() {
        return CompletableFuture.allOf(
                Core.data.reloadConfigurationAsync(),
                Core.config.reloadConfigurationAsync(),
                Core.mysqlModule.reloadConfigurationAsync(),
                Core.redisModule.reloadConfigurationAsync(),
                Core.discordModule.reloadConfigurationAsync(),
                Core.minecraftModule.reloadConfigurationAsync()
        ).thenRunAsync(() -> {
            loadData();
            loadConfig();
            loadMySQLModule();
            loadRedisModule();
            loadDiscordModule();
            loadMinecraftModule();
        }, AsyncExecutor.getService()).thenApply((ok) -> {
            Core.info("Config successfully reloaded");
            return true;
        }).exceptionally((ex) -> {
            Core.severe("Config reload failed", ex);
            return false;
        });
    }

    private static final FileUtils config = Core.config;

    private static void loadConfig() {
        logLevel = config.get("log-level", "INFO");
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
        customMotd = minecraftModule.getBoolean("custom-motd", false);
        hostAllowlist = minecraftModule.getBoolean("host-allowlist", false);
        proxyProtocol = minecraftModule.getBoolean("proxy-protocol", false);
        eventWhitelist = minecraftModule.getBoolean("event-whitelist", false);
        maintenanceMode = minecraftModule.getBoolean("maintenance-mode", false);
        playerHeadAsServerIcon = minecraftModule.getBoolean("ping.icon.use-player-head", false);

        pingVersionName = minecraftModule.get("ping.version-name", "Version 1.x");
        serverDomainName = minecraftModule.get("server-domain", "yourdomain.com");
        verifyServerDomain = minecraftModule.get("manage-connections.verify-server-domain", "verify.yourdomain.com");
        verifyServer = minecraftModule.get("manage-connections.verify-server", "Verify");
        allowedSubnet = minecraftModule.getStringList("security.allow-subnets");
        allowedDomains = minecraftModule.getStringList("security.allowed-domains");

        assignedGuildID = minecraftModule.get("discord.assigned-guild-id");
    }

    private static final FileUtils data = Core.data;

    private static void loadData() {
        maintenanceAccess = new ArrayList<>();
        countingNumbers = new HashMap<>();
        giveawayEndtimes = new HashMap<>();
        whitelistedPlayers = new HashMap<>();
        giveawayParticipantSets = new HashMap<>();
        eligibleUsersForGiveawaySets = new HashMap<>();

        maintenanceAccess.addAll(data.getStringList("minecraft.maintenance-access"));

        guildIDs.forEach(guildID -> {
            countingNumbers.put(guildID, data.getInt("servers." + guildID + ".counting.current-number"));
            giveawayEndtimes.put(guildID, data.getLong("servers." + guildID + ".giveaway.endTime", -1));
            giveawayParticipantSets.put(guildID, new HashSet<>(data.getStringList("servers." + guildID + ".giveaway.participants")));
            eligibleUsersForGiveawaySets.put(guildID, new HashSet<>(data.getStringList("servers." + guildID + ".giveaway.eligible-users")));
        });

        Map<String, Object> raw = data.getMap("minecraft.whitelist", true);
        for (var e : raw.entrySet()) {
            String discordID = e.getKey();
            String minecraftName = e.getValue().toString();
            try {
                whitelistedPlayers.put(discordID, minecraftName);
                Core.info("User " + discordID + " whitelisted player " + minecraftName + " has been found!");
            } catch (Exception ex) {
                Core.severe("Whitelist: Invalid entry - key='" + discordID + "' value='" + minecraftName + "' (" + ex.getMessage() + "). Skipped.");
            }
        }

        VPermissionProvider.clearPermissions();
        ConfigurationSection section = data.getConfigurationSection("minecraft.permissions");

        if(section == null) {
            Core.warn("minecraft.permissions section is null");
            return;
        }

        section.getKeys(false).forEach(playerName ->
                VPermissionProvider.addPermissions(playerName, data.getStringList("minecraft.permissions." + playerName)));
    }


    private static final FileUtils discordModule = Core.discordModule;

    private static void loadDiscordModule() {
        guildIDs = new HashSet<>();
        discordServerProfiles = new HashMap<>();

        discordEnable = discordModule.getBoolean("discord.enabled", false);
        activityType = discordModule.get("discord.activity-type", "");
        activity = discordModule.get("discord.activity", "");
        connectTCPServer = discordModule.getBoolean("discord.tcp-server", false);

        guildIDs = discordModule.getConfigurationSection("servers").getKeys(false);
        guildIDs.forEach(guildId -> discordServerProfiles.put(guildId, new DiscordServerProfile(
                // guildId
                guildId,
                // name
                discordModule.get("servers." + guildId + ".name", guildId),
                // joinRoleId (user-role-id im YAML)
                String.valueOf(discordModule.getLong("servers." + guildId + ".join-role-id", -1)),
                // staffRoleId
                String.valueOf(discordModule.getLong("servers." + guildId + ".staff-role-id", -1)),
                // betaTesterRoleId
                String.valueOf(discordModule.getLong("servers." + guildId + ".beta-tester-role-id", -1)),
                // whitelistedRoleId
                String.valueOf(discordModule.getLong("servers." + guildId + ".whitelisted-role-id", -1)),

                // logChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".log-channel-id", -1)),
                // welcomeChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".welcome-channel-id", -1)),
                // ticketCategoryId
                String.valueOf(discordModule.getLong("servers." + guildId + ".tickets-category-id", -1)),
                // countingChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".counting-channel-id", -1)),
                // giveawayChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".giveaway-channel-id", -1)),
                // whitelistChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".whitelist-channel-id", -1)),
                // inviteLogChannelId
                String.valueOf(discordModule.getLong("servers." + guildId + ".invite-log-channel-id", -1)))));
    }


    public static String getLogLevel() {
        return logLevel;
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

    public static List<String> getMaintenanceAccess() {
        return maintenanceAccess;
    }

    public static Map<String, String> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }


    public static boolean usePlayerHeadAsServerIcon() {
        return playerHeadAsServerIcon;
    }

    public static boolean isEventWhitelist() {
        return eventWhitelist;
    }

    public static boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public static boolean isCustomMotd() {
        return customMotd;
    }

    public static boolean isHostAllowlist() {
        return hostAllowlist;
    }

    public static boolean isProxyProtocol() {
        return proxyProtocol;
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

    public static String getPingVersionName() {
        return pingVersionName;
    }

    public static String getAssignedGuildID() {
        return assignedGuildID;
    }

    public static List<String> getAllowedDomains() {
        return allowedDomains;
    }

    public static List<String> getAllowedSubnet() {
        return allowedSubnet;
    }


    public static boolean isDiscordDisabled() {
        return !discordEnable;
    }

    public static boolean isConnectTCPServer() {
        return connectTCPServer;
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

    public static String getWhitelistedRoleID(String guildID) {
        return discordServerProfiles.get(guildID).getWhitelistedRoleId();
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


    public static void whitelistPlayer(String userId, String minecraftName) {
        whitelistedPlayers.put(userId, minecraftName);
        data.saveAsync("minecraft.whitelist", whitelistedPlayers);
    }

    public static void addMaintenanceAccess(String minecraftName) {
        maintenanceAccess.add(minecraftName);
        data.saveAsync("minecraft.maintenance-access", maintenanceAccess);
    }

    public static void setCountingNumber(String guildId, Integer countingNumber) {
        countingNumbers.put(guildId, countingNumber);
        data.saveAsync("servers." + guildId + ".counting.current-number", countingNumber);
    }

    public static void setGiveawayEndtime(String guildId, long endTime) {
        giveawayEndtimes.put(guildId, endTime);
        data.saveAsync("servers." + guildId + ".giveaway.endTime", endTime);
    }

    public static boolean addGiveawayParticipant(String guildId, String participant) {
        Set<String> set = giveawayParticipantSets.computeIfAbsent(guildId, id -> new HashSet<>());
        boolean added = set.add(participant);
        data.saveAsync("servers." + guildId + ".giveaway.participants", new ArrayList<>(set));
        return added;
    }

    public static void addEligibleUserForGiveaway(String guildId, String userId) {
        var set = eligibleUsersForGiveawaySets.computeIfAbsent(guildId, id -> new HashSet<>());
        set.add(userId);
        data.saveAsync("servers." + guildId + ".giveaway.eligible-users", new ArrayList<>(set));
    }

    public static void setBetaTesterRoleID(String guildID, String roleID) {
        discordServerProfiles.get(guildID).setBetaTesterRoleId(roleID);
        discordModule.saveAsync("servers." + guildID + ".beta-tester-role-id", Long.valueOf(roleID));
    }

    public static void setTicketsCategoryID(String guildID, String ticketCategoryID) {
        discordServerProfiles.get(guildID).setTicketCategoryId(ticketCategoryID);
        discordModule.saveAsync("servers." + guildID + ".tickets-category-id", Long.valueOf(ticketCategoryID));
    }

    public static void setStaffRoleID(String guildID, String staffRoleID) {
        discordServerProfiles.get(guildID).setStaffRoleId(staffRoleID);
        discordModule.saveAsync("servers." + guildID + ".staff-role-id", Long.valueOf(staffRoleID));
    }

    public static void setLogChannelID(String guildID, String logChannelID) {
        discordServerProfiles.get(guildID).setLogChannelId(logChannelID);
        discordModule.saveAsync("servers." + guildID + ".log-channel-id", Long.valueOf(logChannelID));
    }

    public static void setMaintenanceMode(boolean maintenanceMode) {
        Config.maintenanceMode = maintenanceMode;
        minecraftModule.saveAsync("maintenance-mode", maintenanceMode);
    }

    public static void resetLastGiveaway(String guildId) {
        setGiveawayEndtime(guildId, -1L);

        giveawayParticipantSets.computeIfAbsent(guildId, id -> new HashSet<>()).clear();
        eligibleUsersForGiveawaySets.computeIfAbsent(guildId, id -> new HashSet<>()).clear();

        data.saveAsync("servers." + guildId + ".giveaway", null);
    }
}

