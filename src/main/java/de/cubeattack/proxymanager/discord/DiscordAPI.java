package de.cubeattack.proxymanager.discord;

import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.core.TcpServer;
import de.cubeattack.proxymanager.discord.command.*;
import de.cubeattack.proxymanager.discord.listener.CommandListener;
import de.cubeattack.proxymanager.discord.listener.ContextMenuListener;
import de.cubeattack.proxymanager.discord.listener.ReadyListener;
import de.cubeattack.proxymanager.discord.listener.TicketListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
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

import java.util.EnumSet;

public class DiscordAPI extends ListenerAdapter
{

    private JDA JDA;
    private Guild guild;

    public DiscordAPI()
    {
        try
        {
            if(Config.isDiscordDisabled())return;

            JDABuilder jdaBuilder = createDefault(Config.getToken(), Config.getActivityType(), Config.getActivity());
            jdaBuilder.addEventListeners(new InfoCommand());
            jdaBuilder.addEventListeners(new PingCommand());
            jdaBuilder.addEventListeners(new CloseCommand());
            jdaBuilder.addEventListeners(new LookupCommand());
            jdaBuilder.addEventListeners(new ReadyListener());
            jdaBuilder.addEventListeners(new TicketListener());
            jdaBuilder.addEventListeners(new ManagerCommand());
            jdaBuilder.addEventListeners(new CommandListener());
            jdaBuilder.addEventListeners(new ContextMenuListener());

            this.JDA = jdaBuilder.build();
            this.guild = JDA.awaitReady().getGuildById(Config.getGuildID());

            TcpServer.run(Config.getPortTCPServer());
        }
        catch (InterruptedException exception)
        {
            exception.printStackTrace();
        }
    }

    public void loadDiscordCommands() {
        JDA.updateCommands().queue();
        JDA.upsertCommand("ping", "Berechne den Ping des Bots").queue();
        JDA.upsertCommand((Commands.message("Count words"))).queue();
        JDA.upsertCommand(Commands.user("Get user avatar")).queue();

        if(guild != null){
            guild
                    .upsertCommand("info", "Zeige Informationen über das GiantNetwork an")
                    .queue();
            guild
                    .upsertCommand("close", "Schließe ein Ticket")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .queue();
            guild
                    .upsertCommand("lookup", "Zeige Informationen über einen Minecraft Spieler")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                    .addOptions(new OptionData(OptionType.STRING, "name", "Name des Minecraft Spielers", true, true))
                    .queue();
            guild
                    .upsertCommand("manager", "Befehle für Admins / Developer")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .addSubcommands(new SubcommandData( "restart", "Restart"))
                    .addSubcommands(new SubcommandData( "reloadcommands", "Reload Discord Commands"))
                    .addSubcommands(new SubcommandData( "ticketsetup", "Setup TicketBot"))
                    .addSubcommands(new SubcommandData( "closetickets", "Lösche alle Tickets"))
                    .setGuildOnly(true)
                    .queue();
        }
    }

    public JDABuilder createDefault(String token, String activityType, String activity) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);

        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        try {
            jdaBuilder.setActivity(Activity.of(Activity.ActivityType.valueOf(activityType), activity));
        }catch (Exception ex){
            Core.warn("Error whiles loading ActivityType from Config : " + ex.getLocalizedMessage());
        }

        jdaBuilder.setChunkingFilter(ChunkingFilter.ALL);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableCache(EnumSet.of(CacheFlag.ONLINE_STATUS, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.VOICE_STATE));
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGES);

        return jdaBuilder;
    }

    public void shutdown(){
        if(Config.isDiscordDisabled())return;
        getJDA().shutdown();
    }

    public JDA getJDA() {
        return JDA;
    }

    public Guild getGuild() {
        return guild;
    }
}