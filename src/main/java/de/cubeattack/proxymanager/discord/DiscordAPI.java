package de.cubeattack.proxymanager.discord;

import de.cubeattack.proxymanager.ProxyInstance;
import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.core.TcpServer;
import de.cubeattack.proxymanager.discord.command.*;
import de.cubeattack.proxymanager.discord.listener.*;
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

    public DiscordAPI(ProxyInstance proxyInstance) {
        try {
            if (Config.isDiscordDisabled()) return;

            JDA = createDefault(Config.getToken(), Config.getActivityType(), Config.getActivity()).build();
            JDA.addEventListener(
                    new InfoCommand(proxyInstance),
                    new PingCommand(),
                    new CloseCommand(),
                    new LookupCommand(),
                    new ReadyListener(),
                    new TicketListener(this),
                    new ManagerCommand(this),
                    new GiveawayCommand(this),
                    new MessageListener(),
                    new CommandListener(),
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

    public void loadDiscordCommands() {

        Core.warn("Discord commands are reloading yet");

        JDA.updateCommands().complete().forEach(cmd -> JDA.deleteCommandById(cmd.getApplicationIdLong()).queue());
        JDA.updateCommands().queue();
        JDA.upsertCommand("ping", "Berechne den Ping des Bots").queue();
        // JDA.upsertCommand((Commands.message("Count words"))).queue();
        // JDA.upsertCommand(Commands.user("Get user avatar")).queue();

        if (guild != null) {
            guild
                    .upsertCommand("info", "Zeige Informationen über das Server an")
                    .queue();
            guild
                    .upsertCommand("close", "Schließe ein Ticket")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .addOptions(new OptionData(OptionType.STRING, "reason", "Grund für die Schließung", false, false))
                    .queue();
            guild
                    .upsertCommand("lookup", "Zeige Informationen über einen Minecraft Spieler")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .addOptions(new OptionData(OptionType.STRING, "name", "Name des Minecraft Spielers", true, false))
                    .queue();
            guild
                    .upsertCommand("giveaway", "Giveaway steuern")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .addSubcommands(
                            new SubcommandData("start", "Starte ein neues Giveaway")
                                    .addOption(OptionType.INTEGER, "dauer", "Dauer in Stunden", true)
                                    .addOption(OptionType.INTEGER, "delay", "Verzögerung in Stunden"),
                            new SubcommandData("info", "Giveaway infos anzeigen"),
                            new SubcommandData("cancel", "Giveaway abbrechen")
                    )
                    .queue();
            guild
                    .upsertCommand("manager", "Befehle für Admins / Developer")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .addSubcommands(new SubcommandData("restart", "Restart alle Services"))
                    .addSubcommands(new SubcommandData("reload-commands", "Reloade alle Discord Commands"))
                    .addSubcommands(new SubcommandData("ticket-setup", "Starte das Setup des Ticket-Bots"))
                    .addSubcommands(new SubcommandData("close-all-tickets", "Lösche alle offenen Tickets"))
                    .queue();
        }
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
        getJDA().shutdown();
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