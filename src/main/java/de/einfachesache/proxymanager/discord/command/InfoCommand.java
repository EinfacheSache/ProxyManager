package de.einfachesache.proxymanager.discord.command;

import de.cubeattack.api.util.RuntimeUsageUtils;
import de.einfachesache.proxymanager.ProxyInstance;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

public class InfoCommand extends ListenerAdapter {

    private final ProxyInstance proxyInstance;

    public InfoCommand(ProxyInstance proxyInstance) {
        this.proxyInstance = proxyInstance;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getName().equalsIgnoreCase("info")) return;

        event.deferReply().queue();

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();
        embedBuilder.setTitle(Config.getServerName() + " Informationen");
        embedBuilder.setColor(Color.GREEN);

        long currentTime = System.currentTimeMillis();
        boolean isPinged = isReachable(Config.getServerDomainName().toLowerCase(), 25565, 2000);
        currentTime = System.currentTimeMillis() - currentTime;

        String strBasic = "IP: " + Config.getServerDomainName() + "\n" +
                "Version: 1.21.4+ \n" +
                "Ping: " + (isPinged ? currentTime + "ms" : "No Connection") + "\n" +
                "Spieler Online: " + proxyInstance.getOnlinePlayerCount() + "/" + proxyInstance.getPlayerLimit() + "\n" +
                "Up-Time: " + Duration.ofMillis(System.currentTimeMillis() - Core.UPTIME).toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase() + "\n";

        embedBuilder.addField("Basic Information", strBasic, false);

        embedBuilder.addField("Ram Verbrauch", RuntimeUsageUtils.getSystemUsedRam() + " / " + RuntimeUsageUtils.getSystemMaxRam() + "MB", false);
        embedBuilder.addField("CPU Verbrauch", BigDecimal.valueOf(RuntimeUsageUtils.getCpuUsage()).setScale(2, RoundingMode.HALF_UP) + "%", false);

        final StringBuilder strServer = new StringBuilder();

        proxyInstance.getBackendServerAsString().stream().filter(backendServer -> !backendServer.serverName().equals("fallback")).forEach((serverInfo) ->
                strServer.append("**-**⠀").append(serverInfo.serverName()).append("⠀->⠀Spieler Online: ").append(serverInfo.playerCount()).append("\n"));

        embedBuilder.addField("Servers:", strServer.toString(), false);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();

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
