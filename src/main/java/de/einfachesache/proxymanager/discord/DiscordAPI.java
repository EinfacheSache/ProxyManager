package de.einfachesache.proxymanager.discord;

import de.einfachesache.proxymanager.ProxyInstance;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.TcpServer;
import de.einfachesache.proxymanager.discord.command.*;
import de.einfachesache.proxymanager.discord.listener.*;
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

public class DiscordAPI extends ListenerAdapter {

    private JDA JDA;
    private Guild guild;

    public static final long DEV_USER_ID = 571736032165232651L;

    public DiscordAPI(ProxyInstance proxyInstance) {
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
                    new ReadyListener(),
                    new TicketCommand(this),
                    new TicketListener(this),
                    new GiveawayCommand(this),
                    new MessageListener(),
                    new ContextMenuListener(),
                    new BotGuildJoinListener(),
                    new MemberJoinGuildListener()
            );

            JDA.awaitReady();

            guild = JDA.getGuildById(Config.getGuildID());

            TcpServer.run(Config.getPortTCPServer());

        } catch (InterruptedException exception) {
            Core.severe("Error initializing Discord API", exception);
        }
    }


    public void loadGlobalDiscordCommands() {

        Core.info("Global development commands are reloading yet");

        JDA.updateCommands()
                .addCommands(
                        Commands.slash("core", "Befehle für Developer")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                                .addSubcommands(new SubcommandData("restart", "Restart alle Services"))
                                .addSubcommands(new SubcommandData("reload-commands", "Reloade alle Discord Commands"))
                                .addSubcommands(new SubcommandData("reload-commands-global", "Reloade alle Discord Commands Global"))
                                .addSubcommands(new SubcommandData("register", "Registers this server and enables the bot to operate on it")),
                        Commands.slash("ping", "Berechne den Ping des Bots")
                )
                .queue();
    }

    public void loadGuildDiscordCommands(Guild guild) {

        if (guild == null) {
            Core.severe("Guild could not be found");
            return;
        }

        Core.info("Commands on " + guild.getName() + " are reloading yet");

        guild.updateCommands()
                .addCommands(
                        Commands.slash("info", "Zeige Informationen über das Server an"),

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

    public Guild getGuild() {
        return guild;
    }

    public Role getStaffRole() {
        String staffRoleID = Config.getStaffRoleID();
        if (staffRoleID.isEmpty() || guild.getRoleById(staffRoleID) == null) {
            staffRoleID = guild.createRole().setName("Staff").setColor(Color.GREEN).complete().getId();
            Config.setTeamRoleID(staffRoleID);
        }

        return guild.getRoleById(staffRoleID);
    }

    public Category getTicketCategory() {
        if (Config.getTicketsCategoryID().isEmpty() || guild.getCategoryById(Config.getTicketsCategoryID()) == null) {
            Category category = guild.createCategory("[Tickets]").complete();
            category.getManager()
                    .putRolePermissionOverride(Long.parseLong(getStaffRole().getId()), EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
            Config.setCategoryID(category.getId());
        }
        return guild.getCategoryById(Config.getTicketsCategoryID());
    }

    public TextChannel getDiscordLogChannel() {

        if (guild != null && (Config.getLogChannelID().isEmpty() || guild.getTextChannelById(Config.getLogChannelID()) == null)) {
            TextChannel textChannel = guild.createTextChannel("\uD83D\uDCBE│server-logs").complete();
            Config.setLogChannelID(textChannel.getId());
        }

        return JDA.getTextChannelById(Config.getLogChannelID());
    }
}