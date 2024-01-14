package de.cubeattack.proxymanager.bungee.listener;

import de.cubeattack.api.API;
import de.cubeattack.proxymanager.bungee.ScreenBuilder;
import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
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
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;


@SuppressWarnings("deprecation")
public class ManageConnection implements Listener {
    private final HashMap<String, Favicon> images = new HashMap<>();
    ServerPing.Protocol version;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFirstPing(ProxyPingEvent e) {
        int versionNumber = e.getResponse().getVersion().getProtocol();
        version = new ServerPing.Protocol("1.8 - 1.20.4 (Eric ist zu fett für den Server)", versionNumber);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent e) {
        try(Jedis jedis = Core.getRedisConnector().getJedisPool().getResource()) {
            if (!jedis.isConnected() || jedis.isBroken()) {
                return;
            }
            API.getExecutorService().submit(() -> jedis.set(e.getConnection().getAddress().getHostString(), e.getConnection().getName()));
            API.getExecutorService().submit(() -> images.put(e.getConnection().getName(), getFavicon(e.getConnection().getName())));
        }
    }

    @EventHandler
    public void initialServerConnect(PostLoginEvent event) {
        if(!Config.isMaintenanceMode())
            return;

        if(event.getPlayer().hasPermission("proxy.maintenance.bypass"))
            return;

        event.getPlayer().getPendingConnection().disconnect(new ScreenBuilder()
                .addLine("§cServer Maintenance Alert")
                .addLine("§cSorry, but our server is currently under maintenance.")
                .addLine("§ePlease check our Discord for updates:")
                .addLine("§7§n§o§nwww.giantnetwork.de/discord")
                .build());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent e) {
        if(e.getConnection().getVirtualHost() == null) return;
        String serverHost = e.getConnection().getVirtualHost().getHostName();

        for (String allowedDomain: Config.getAllowedDomains()) {
            if(allowedDomain.equalsIgnoreCase(serverHost))return;
            if(allowedDomain.startsWith("*") && serverHost.toLowerCase().endsWith(allowedDomain.toLowerCase().replace("*.", "")))return;
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

        String line1 = "§7§kKK§r §6Willkommen auf §l" + Config.getServerDomainName() + "§c§o [1.8-1.20.4] §7§kKK§r";
        String line2 = "";
        String playerName = null;

        if (Config.isPlayerHeadAsServerIcon()){

            try(Jedis jedis = Core.getRedisConnector().getJedisPool().getResource()) {
                if (jedis.isConnected() && !jedis.isBroken()) {
                    playerName = jedis.get(e.getConnection().getAddress().getHostString());
                    if (images.containsKey(playerName)) {
                        e.getResponse().setFavicon(images.get(playerName));
                    }
                }

                if (e.getConnection().getVirtualHost() != null && e.getConnection().getVirtualHost().getHostName().toLowerCase().startsWith("builder.")) {
                    line2 = "          §6Eric scheißt auf unsere Builder :)";
                } else if (playerName != null) {
                    line2 = "§6Willkommen §b" + playerName + "§6 auf " + Config.getServerDomainName();
                } else {
                    line2 = "            §2§lEarth Server §7+ §b§lAlpha Test";
                }
            }
        }

        e.getResponse().setDescriptionComponent(new TextComponent(line1 + "\n" + line2));
        e.getResponse().setVersion(version);
    }

    private Favicon getFavicon(String playerName){
        Favicon favicon = null;
        try {
            favicon = Favicon.create(ImageIO.read(new URL("https://minotar.net/helm/" + playerName + "/64.png")));
        } catch (IOException ignored) {}
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
