package de.einfachesache.proxymanager.bungee.listener;

import de.cubeattack.api.API;
import de.einfachesache.proxymanager.bungee.ScreenBuilder;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.MOTDUtils;
import de.einfachesache.proxymanager.core.RedisConnector;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;


@SuppressWarnings("deprecation")
public class ManageConnection implements Listener {
    private final HashMap<String, Favicon> images = new HashMap<>();
    ServerPing.Protocol version;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFirstPing(ProxyPingEvent e) {
        int versionNumber = e.getResponse().getVersion().getProtocol();
        version = new ServerPing.Protocol("Version 1.21.x", versionNumber);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent e) {
        RedisConnector jedis = Core.getRedisConnector();

        API.getExecutorService().submit(() -> jedis.set(e.getConnection().getAddress().getHostString(), e.getConnection().getName()));
        API.getExecutorService().submit(() -> images.put(e.getConnection().getName(), getFavicon(e.getConnection().getName())));
    }

    @EventHandler
    public void initialServerConnect(PostLoginEvent event) {
        if (!Config.isMaintenanceMode())
            return;

        if (event.getPlayer().hasPermission("proxy.maintenance.bypass"))
            return;

        event.getPlayer().getPendingConnection().disconnect(new ScreenBuilder()
                .addLine("§4§lWe are currently in maintenance\n")
                .addLine("§7Discord: §b" + Config.getServerDomainName() + "/discord")
                .build());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent e) {
        if (e.getConnection().getVirtualHost() == null) return;
        String serverHost = e.getConnection().getVirtualHost().getHostName();
        String localHost = ((InetSocketAddress) e.getConnection().getSocketAddress()).getHostString();

        if (localHost.startsWith("192.168.178.")) {
            return;
        }

        for (String allowedDomain : Config.getAllowedDomains()) {
            if (allowedDomain.equalsIgnoreCase(serverHost)) return;
            if (allowedDomain.startsWith("*") && serverHost.toLowerCase().endsWith(allowedDomain.toLowerCase().replace("*.", "")))
                return;
        }

        ScreenBuilder builder = new ScreenBuilder()
                .addLine("§4-----------------------------")
                .addLine("§4Login over " + serverHost + " is not allowed")
                .addLine("§4Please join over " + Config.getServerDomainName())
                .addLine("§4-----------------------------");

        e.getConnection().disconnect(builder.build());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(ProxyPingEvent e) {

        if (e.getConnection() == null) return;

        String line1 = "§7§kKK§r §2§l100 Spieler Events §7§l| §b§lAlpha Test §c[1.21.x] §7§kKK§r";
        String line2 =  MOTDUtils.getCenteredMessage("§7§kK§r §6Willkommen auf §l" + Config.getServerDomainName() + " §7§kK§r");

        if (Config.isManageConnectionEnabled()) {
            RedisConnector jedis = Core.getRedisConnector();
            String playerName = jedis.get(e.getConnection().getAddress().getHostString());

            if(Config.isPlayerHeadAsServerIcon()) {
                if (images.containsKey(playerName)) {
                    e.getResponse().setFavicon(images.get(playerName));
                }
            }

            if (e.getConnection().getVirtualHost() != null && e.getConnection().getVirtualHost().getHostName().toLowerCase().startsWith("builder.")) {
                line2 = "          §6Chaya aka Beda scheißt auf unsere Builder :)";
            } else if (playerName != null) {
                line2 = MOTDUtils.getCenteredMessage("§7§kK §6Willkommen §b" + playerName + "§6 auf " + Config.getServerDomainName()) + " §7§kK";
            }
        }

        e.getResponse().setDescriptionComponent(new TextComponent(line1 + "\n" + line2));
        e.getResponse().setVersion(version);
    }

    private Favicon getFavicon(String playerName) {
        Favicon favicon = null;
        try {
            favicon = Favicon.create(ImageIO.read(new URL("https://minotar.net/helm/" + playerName + "/64.png")));
        } catch (IOException ignored) {
        }
        return favicon;
    }
}



    /*@EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent e) {
        Socket socket = new Socket();
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(Config.getVerifyServer());

        if (!e.getPlayer().getPendingConnection().getVirtualHost().getHostName().equalsIgnoreCase(Config.getVerifyServerDomain())) return;
        if (serverInfo == null) return;
        if (serverInfo.equals(e.getTarget())) return;

        try {
            socket.connect(serverInfo.getSocketAddress());
            socket.close();

            if (e.getPlayer().getServer() != null){
                if(ProxyServer.getInstance().getServerInfo(Config.getVerifyServer()).equals(e.getPlayer().getServer().getInfo())){
                    e.setCancelled(true);
                    return;
                }
            }
            e.setTarget(serverInfo);

            return;
        } catch (IOException ignored) {}

        e.getPlayer().disconnect(new TextComponent("§4Server not found\n§4Verifikations Server konnte nicht gefunden werden"));

    }*/
