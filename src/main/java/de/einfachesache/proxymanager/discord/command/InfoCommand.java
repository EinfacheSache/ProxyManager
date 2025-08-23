package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.api.util.RuntimeUsageUtils;
import de.einfachesache.proxymanager.velocity.ProxyInstance;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

        String strBasic = "IP: " + Config.getServerDomainName() + "\n" +
                "Version: 1.21.5+ \n" +
                "Status: " + (isOnline(Config.getServerDomainName().toLowerCase()) ? "Online" : "Offline") + "\n" +
                "Spieler: " + proxyInstance.getOnlinePlayerCount() + "/" + proxyInstance.getPlayerLimit() + "\n" +
                "Up-Time: " + Duration.ofMillis(System.currentTimeMillis() - Core.UPTIME).toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase() + "\n";

        embedBuilder.addField("Basic Information", strBasic, false);

        embedBuilder.addField("Ram Verbrauch", RuntimeUsageUtils.getHostUsedRamMB() + " / " + RuntimeUsageUtils.getHostMaxRamMB() + "MB", false);
        embedBuilder.addField("CPU Verbrauch", BigDecimal.valueOf(RuntimeUsageUtils.getCpuUsage()).setScale(2, RoundingMode.HALF_UP) + "%", false);

        final StringBuilder strServer = new StringBuilder();

        proxyInstance.getBackendServerAsString().stream().filter(backendServer -> !backendServer.serverName().equals("fallback")).forEach((serverInfo) ->
                strServer.append("**-**⠀").append(serverInfo.serverName()).append("⠀->⠀Spieler Online: ").append(serverInfo.playerCount()).append("\n"));

        embedBuilder.addField("Servers:", strServer.toString(), false);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();

    }

    public static boolean isOnline(String host) {
        String url = "https://api.mcsrvstat.us/simple/" + host;
        try (HttpClient http = HttpClient.newHttpClient()){
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<Void> res = http.send(req, HttpResponse.BodyHandlers.discarding());
            return res.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}