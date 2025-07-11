package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.api.util.RuntimeUsageUtils;
import de.cubeattack.proxymanager.bungee.BungeeProxyManager;
import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.time.Duration;
import java.util.Objects;

public class InfoCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("info")) return;

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();
        embedBuilder.setTitle("GianNetwork Informationen");
        embedBuilder.setColor(Color.GREEN);

        long currentTime = System.currentTimeMillis();
        boolean isPinged = isReachable("mc.einfachesache.de", 25565, 2000);
        currentTime = System.currentTimeMillis() - currentTime;

        String strBasic = "IP: " + Config.getServerDomainName() + "\n" +
                "Version: 1.21.4+ \n" +
                "Ping: " + (isPinged ? currentTime + "ms" : "No Connection") + "\n" +
                "Spieler Online: " + BungeeProxyManager.getPlugin().getProxy().getOnlineCount() + "/" + BungeeProxyManager.getPlugin().getProxy().getConfig().getPlayerLimit() + "\n" +
                "Up-Time: " + Duration.ofMillis(System.currentTimeMillis() - Core.UPTIME).toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase() + "\n";

        embedBuilder.addField("Basic Information", strBasic, false);

        embedBuilder.addField("Ram Verbrauch", RuntimeUsageUtils.getSystemUsedRam() + " / " + RuntimeUsageUtils.getSystemMaxRam() + "MB", false);
        embedBuilder.addField("CPU Verbrauch", BigDecimal.valueOf(RuntimeUsageUtils.getCpuUsage()).setScale(2, RoundingMode.HALF_UP) + "%", false);

        final StringBuilder strServer = new StringBuilder();

        BungeeProxyManager.getPlugin().getProxy().getServers().values().stream().filter(values -> !values.getName().equals("fallback")).forEach((serverInfo) ->
                strServer.append("**-**⠀").append(serverInfo.getName()).append("⠀->⠀Spieler Online: ").append(serverInfo.getPlayers().size()).append("\n"));

        embedBuilder.addField("Servers:", strServer.toString(), false);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();

    }

    public static boolean isReachable(String host, int port, int timeoutMillis) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(Inet4Address.getByName(host).getHostAddress(), port), timeoutMillis);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
