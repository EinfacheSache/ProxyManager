package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.MOTDUtils;
import de.einfachesache.proxymanager.core.RedisConnector;
import net.kyori.adventure.text.Component;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyPingListener {

    private final Map<String, Favicon> images = new ConcurrentHashMap<>();

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {

        if (!Config.usePlayerHeadAsServerIcon()) return;

        RedisConnector jedis = Core.getRedisConnector();
        String address = getPlayerAddress(event.getConnection());
        String username = event.getUsername();

        AsyncExecutor.getService().submit(() -> jedis.set(address, username));
        AsyncExecutor.getService().submit(() -> images.put(username, getFavicon(username)));
    }


    @Subscribe
    public void onPing(ProxyPingEvent event) {
        var connection = event.getConnection();

        RedisConnector jedis = Core.getRedisConnector();
        String playerName = jedis.get(getPlayerAddress(connection));

        String line1 = MOTDUtils.getCenteredMessage("§7§kKK§r §5§lHalloween Event §8| §6§lBattle Royale §8[§c1.21.x§8] §7§kKK§r");
        String line2 = MOTDUtils.getCenteredMessage("§7§kK§r §2§l§o100 Spieler §8• §6Willkommen auf " + Config.getServerDomainName() + " §7§kK§r");

        ServerPing original = event.getPing();
        ServerPing.Builder builder = ServerPing.builder();
        ServerPing.Version version = new ServerPing.Version(original.getVersion().getProtocol(), Config.getPingVersionName());

        builder
                .version(version)
                .samplePlayers(original.asBuilder().getSamplePlayers())
                .onlinePlayers(original.getPlayers().orElseThrow().getOnline())
                .maximumPlayers(original.getPlayers().get().getMax());

        if (Config.usePlayerHeadAsServerIcon() && playerName != null && images.containsKey(playerName)) {
            builder.favicon(images.get(playerName));
        } else if (original.getFavicon().isPresent()) {
            builder.favicon(original.getFavicon().get());
        }

        if (connection.getVirtualHost().isPresent() && connection.getVirtualHost().get().getHostName().toLowerCase().startsWith("builder.")) {
            line2 = MOTDUtils.getCenteredMessage("§6Chaya aka Beda scheißt auf unsere Builder :)");
        } else if (playerName != null) {
            line2 = MOTDUtils.getCenteredMessage("§7§kK §6Willkommen §b" + playerName + "§6 auf " + Config.getServerDomainName()) + " §7§kK";
        }

        builder.description(Component.text(line1 + "\n" + line2));

        event.setPing(builder.build());
    }

    private Favicon getFavicon(String playerName) {
        try {
            return Favicon.create(ImageIO.read(new URI("https://minotar.net/helm/" + playerName + "/64.png").toURL()));
        } catch (URISyntaxException | IOException ignored) {
            return null;
        }
    }

    private String getPlayerAddress(InboundConnection connection) {
        return connection.getRemoteAddress().getHostString();
    }
}
