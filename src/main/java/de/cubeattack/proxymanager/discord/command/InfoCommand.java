package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.api.util.RuntimeUsageUtils;
import de.cubeattack.proxymanager.bungee.ProxyManager;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.Socket;
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
        boolean isPinged = isReachable();
        currentTime = System.currentTimeMillis() - currentTime;

        String strBasic = "IP: GiantNetwork.de\n" +
                "Version: 1.8.9 - 1.20.2\n" +
                "Ping: " + (isPinged ? currentTime + "ms" : "No Connection") + "\n" +
                "Spieler Online: " + ProxyManager.getPlugin().getProxy().getOnlineCount() + "/" + ProxyManager.getPlugin().getProxy().getConfig().getPlayerLimit() + "\n" +
                "Up-Time: " + Duration.ofMillis(System.currentTimeMillis() - ProxyManager.getUptime()).toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase() + "\n";

        embedBuilder.addField("Basic Information", strBasic, false);

        embedBuilder.addField("Ram Verbrauch", RuntimeUsageUtils.getSystemUsedRam() + " / " + RuntimeUsageUtils.getSystemMaxRam() + "MB", false);
        embedBuilder.addField("CPU Verbrauch", BigDecimal.valueOf(RuntimeUsageUtils.getCpuUsage()).setScale(2, RoundingMode.HALF_UP) + "%", false);

        final StringBuilder strServer = new StringBuilder();

        ProxyManager.getPlugin().getProxy().getServersCopy().values().stream().filter(values -> !values.getName().equals("fallback")).forEach((serverInfo) ->
                strServer.append("  **-** ").append(serverInfo.getName()).append(" : Spieler Online: ").append(serverInfo.getPlayers().size()).append("\n"));

        embedBuilder.addField("Servers:", strServer.toString(), false);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();

    }

    private static boolean isReachable() {
        try (Socket soc = new Socket()) {
            soc.connect(new InetSocketAddress("giantnetwork.de", 80), 1500);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
