package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import de.cubeattack.api.API;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.MOTDUtils;
import de.einfachesache.proxymanager.core.RedisConnector;
import de.einfachesache.proxymanager.velocity.ScreenBuilder;
import net.kyori.adventure.text.Component;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class ManageConnection {

    private final Map<String, Favicon> images = new ConcurrentHashMap<>();

    private ServerPing.Version version;

    @Subscribe(order = PostOrder.FIRST)
    public void onFirstPing(ProxyPingEvent event) {
        int protocol = event.getPing().getVersion().getProtocol();
        version = new ServerPing.Version(protocol, "Version 1.21.x");
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        RedisConnector jedis = Core.getRedisConnector();
        String address = event.getConnection().getRemoteAddress().getHostString();
        String username = event.getUsername();

        API.getExecutorService().submit(() -> jedis.set(address, username));
        API.getExecutorService().submit(() -> images.put(username, getFavicon(username)));
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        if (!Config.isMaintenanceMode() || player.hasPermission("proxy.maintenance.bypass")) {
            return;
        }
        Component kickMsg = new ScreenBuilder()
                .addLine("§4§lWe are currently in maintenance\n")
                .addLine("§7Discord: §b" + Config.getServerDomainName() + "/discord")
                .build();
        player.disconnect(kickMsg);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onLogin(LoginEvent event) {
        var connection = event.getPlayer();
        if (connection.getVirtualHost().isEmpty()) {
            return;
        }
        String serverHost = connection.getVirtualHost().get().getHostName();
        String localHost = connection.getRemoteAddress().getHostString();

        if (localHost.startsWith("192.168.178.")) {
            return;
        }

        for (String allowed : Config.getAllowedDomains()) {
            if (allowed.equalsIgnoreCase(serverHost)
                    || (allowed.startsWith("*.") && serverHost.toLowerCase().endsWith(allowed.substring(2).toLowerCase()))) {
                return;
            }
        }

        Component kickMsg = new ScreenBuilder()
                .addLine("§4-----------------------------")
                .addLine("§4Login over " + serverHost + " is not allowed")
                .addLine("§4Please join over " + Config.getServerDomainName())
                .addLine("§4-----------------------------")
                .build();
        connection.disconnect(kickMsg);
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        var connection = event.getConnection();
        if (connection == null) {
            return;
        }

        String line1 = "§7§kKK§r §2§l100 Spieler Events §7§l| §b§lAlpha Test §c[1.21.x] §7§kKK§r";
        String line2 = MOTDUtils.getCenteredMessage("§7§kK§r §6Willkommen auf §l" + Config.getServerDomainName() + " §7§kK§r");

        ServerPing original = event.getPing();
        ServerPing.Builder builder = ServerPing.builder();

        builder
                .version(version)
                .samplePlayers(original.asBuilder().getSamplePlayers())
                .onlinePlayers(original.getPlayers().orElseThrow().getOnline())
                .maximumPlayers(original.getPlayers().get().getMax());

        if (original.getFavicon().isPresent()) {
            builder.favicon(original.getFavicon().get());
        }

        if (Config.isManageConnectionEnabled()) {
            String address = connection.getRemoteAddress().getHostString();
            RedisConnector jedis = Core.getRedisConnector();
            String playerName = jedis.get(address);

            if (Config.isPlayerHeadAsServerIcon() && images.containsKey(playerName)) {
                builder.favicon(images.get(playerName));
            }

            if (connection.getVirtualHost().isPresent()
                    && connection.getVirtualHost().get().getHostName().toLowerCase().startsWith("builder.")) {
                line2 = "          §6Chaya aka Beda scheißt auf unsere Builder :)";
            } else if (playerName != null) {
                line2 = MOTDUtils.getCenteredMessage("§7§kK §6Willkommen §b" + playerName + "§6 auf " + Config.getServerDomainName()) + " §7§kK";
            }
        }

        builder.description(Component.text(line1 + "\n" + line2));

        event.setPing(builder.build());
    }

    private Favicon getFavicon(String playerName) {
        try {
            return Favicon.create(ImageIO.read(
                    new URL("https://minotar.net/helm/" + playerName + "/64.png")
            ));
        } catch (IOException ignored) {
            return null;
        }
    }
}
