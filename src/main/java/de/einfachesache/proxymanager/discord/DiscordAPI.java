package de.einfachesache.proxymanager.discord;

import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.TcpServer;
import de.einfachesache.proxymanager.discord.command.*;
import de.einfachesache.proxymanager.discord.listener.*;
import de.einfachesache.proxymanager.velocity.ProxyInstance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiscordAPI extends ListenerAdapter {

    private JDA JDA;
    private final Map<String, Guild> guilds = new HashMap<>();

    public static final long DEV_USER_ID = 571736032165232651L;

    public void init(ProxyInstance proxyInstance) {
        try {
            if (Config.isDiscordDisabled()) return;

            JDA = createDefault(Config.getToken(), Config.getActivityType(), Config.getActivity()).build();
            JDA.addEventListener(
                    new CommandListener(),
                    new CoreCommand(),
                    new InfoCommand(proxyInstance),
                    new PingCommand(),
                    new CloseCommand(),
                    new LookupCommand(),
                    new ReadyListener(this),
                    new TicketCommand(this),
                    new TicketListener(this),
                    new GiveawayCommand(this),
                    new WhitelistCommand(this),
                    new MessageListener(),
                    new ContextMenuListener(),
                    new BotGuildJoinListener(),
                    new MemberJoinGuildListener(),
                    new MemberLeaveGuildListener()
            );

            TcpServer.run();

        } catch (InterruptedException exception) {
            Core.severe("Error initializing Discord API", exception);
        }
    }

    public CompletableFuture<Boolean> reloadGuildsAsync() {
        return CompletableFuture
                .runAsync(() -> {
                    loadGlobalDiscordCommands();
                    this.getGuilds().forEach((id, guild) -> this.loadGuildDiscordCommands(guild));
                }, AsyncExecutor.getService())
                .thenApply(v -> {
                    Core.info("Discord commands successfully reloaded");
                    return true;
                })
                .exceptionally(ex -> {
                    Core.severe("Discord command reload failed", ex);
                    return false;
                });
    }

    public void loadGlobalDiscordCommands() {

        Core.info("Global development commands are reloading yet");

        JDA.updateCommands()
                .addCommands(
                        Commands.slash("core", "Befehle für Developer")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                                .addSubcommands(new SubcommandData("restart", "Restart alle Services"))
                                .addSubcommands(new SubcommandData("invite-link", "Generiere Invite Link für den Discord"))
                                .addSubcommands(new SubcommandData("reload-commands", "Reloade alle Discord Commands"))
                                .addSubcommands(new SubcommandData("reload-commands-global", "Reloade alle Discord Commands Global"))
                                .addSubcommands(new SubcommandData("register", "Registers this server and enables the bot to operate on it"))
                                .addSubcommands(new SubcommandData("leave-confirm", "Entferne den Bot vom aktuellen Discord Server")),
                        Commands.slash("ping", "Berechne den Ping des Bots")
                )
                .queue();
    }

    public void loadGuildDiscordCommands(Guild guild) {

        if (guild == null) {
            Core.severe("Guild could not be found");
            return;
        }

        Core.info(guild.getName() + " | Commands are reloading yet");

        guild.updateCommands()
                .addCommands(
                        Commands.slash("info", "Zeige Informationen über das Server an"),

                        Commands.slash("whitelist", "Whitelist dich für den Server")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "name", "Gebe dein Spielername an", true)
                                ),

                        Commands.slash("whitelist-list", "Zeige alle Whitelisted Spieler an")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)),

                        Commands.slash("whitelist-sync", "Whitelist-Sync – Rolle gemäß Konfiguration synchronisieren")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)),

                        Commands.slash("close", "Schließe ein Ticket")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                                .addOptions(
                                        new OptionData(OptionType.STRING, "reason", "Grund für die Schließung", false)
                                ),

                        Commands.slash("lookup", "Zeige Informationen über einen Minecraft Spieler")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                                .addOptions(
                                        new OptionData(OptionType.STRING, "name", "Name des Minecraft Spielers", true)
                                ),

                        Commands.slash("giveaway", "Giveaway steuern")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                                .addSubcommands(
                                        new SubcommandData("start", "Starte ein neues Giveaway")
                                                .addOption(OptionType.INTEGER, "dauer", "Dauer in Stunden", true)
                                                .addOption(OptionType.INTEGER, "delay", "Verzögerung in Stunden", false),
                                        new SubcommandData("info", "Giveaway infos anzeigen"),
                                        new SubcommandData("cancel", "Giveaway abbrechen")
                                ),

                        Commands.slash("ticket", "Befehle für Admins / Developer")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                                .addSubcommands(
                                        new SubcommandData("setup", "Starte das Setup des Ticket-Bots"),
                                        new SubcommandData("close-all", "Lösche alle offenen Tickets")
                                )
                )
                .queue();
    }

    public JDABuilder createDefault(String token, String activityType, String activity) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);

        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        try {
            jdaBuilder.setActivity(Activity.of(Activity.ActivityType.valueOf(activityType), activity));
        } catch (Exception ex) {
            Core.warn("Error whiles loading ActivityType from Config : " + ex.getLocalizedMessage());
        }

        jdaBuilder.setChunkingFilter(ChunkingFilter.ALL);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableCache(EnumSet.of(CacheFlag.EMOJI /*, CacheFlag.ONLINE_STATUS, CacheFlag.CLIENT_STATUS, CacheFlag.VOICE_STATE */));
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES /* GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES , GatewayIntent.DIRECT_MESSAGE_TYPING, */);

        return jdaBuilder;
    }

    public void shutdown() {
        if (Config.isDiscordDisabled()) return;
        JDA.shutdown();
    }


    public JDA getJDA() {
        return JDA;
    }

    public Guild getGuild(String guildID) {
        return guilds.get(guildID);
    }

    public Map<String, Guild> getGuilds() {
        return guilds;
    }

    public Role getStaffRole(String guildID) {
        Guild guild = getGuild(guildID);
        String staffRoleID = Config.getStaffRoleID(guildID);
        if (staffRoleID.isEmpty() || guild.getRoleById(staffRoleID) == null) {
            staffRoleID = guild.createRole().setName("Staff").setColor(Color.GREEN).complete().getId();
            Config.setStaffRoleID(guildID, staffRoleID);
        }

        return guild.getRoleById(staffRoleID);
    }

    public Category getTicketCategory(String guildID) {
        Guild guild = guilds.get(guildID);
        if (Config.getTicketsCategoryID(guildID).isEmpty() || guild.getCategoryById(Config.getTicketsCategoryID(guildID)) == null) {
            Category category = guild.createCategory("[Tickets]").complete();
            category.getManager()
                    .putRolePermissionOverride(Long.parseLong(getStaffRole(guildID).getId()), EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
            Config.setTicketsCategoryID(guildID, category.getId());
        }
        return guild.getCategoryById(Config.getTicketsCategoryID(guildID));
    }

    public TextChannel getDiscordLogChannel(String guildID) {
        Guild guild = guilds.get(guildID);
        if (guild != null && (Config.getLogChannelID(guildID).isEmpty() || guild.getTextChannelById(Config.getLogChannelID(guildID)) == null)) {
            TextChannel textChannel = guild.createTextChannel("\uD83D\uDCBE│server-logs").complete();
            Config.setLogChannelID(guildID, textChannel.getId());
        }

        return JDA.getTextChannelById(Config.getLogChannelID(guildID));
    }
}