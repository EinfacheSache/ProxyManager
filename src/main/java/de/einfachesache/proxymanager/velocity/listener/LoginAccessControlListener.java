package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.velocity.ScreenBuilder;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LoginAccessControlListener {

    private static final String EVENT_SERVER = "Event";
    private static final String FALLBACK_SERVER = "Limbo";
    private static final String BYPASS_PERMISSION_MAINTENANCE = "proxy.bypass.maintenance";
    private static final Component DENY_MESSAGE = Component.text("⛔ Du bist nicht whitelisted » /whitelist im Discord", NamedTextColor.RED);
    private static final Component EVENT_DENY_MESSAGE = Component.text("⛔ Du bist fürs Event nicht whitelisted » /whitelist im Discord", NamedTextColor.RED);

    private static VProxyManager proxy;

    public LoginAccessControlListener(VProxyManager proxy) {
        LoginAccessControlListener.proxy = proxy;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        if (!Config.isMaintenanceMode() || hasMaintenanceAccess(player)) {
            return;
        }

        event.setResult(ResultedEvent.ComponentResult.denied(ScreenBuilder.getMaintenanceScreen()));
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {

        var player = event.getPlayer();
        var originalServer = event.getOriginalServer();

        if (!Config.isEventWhitelist()) return;
        if (hasWhitelistAccess(player)) return;
        if (originalServer.getServerInfo().getName().equalsIgnoreCase(FALLBACK_SERVER)) return;

        Component denyMessage = originalServer.getServerInfo().getName().toLowerCase().contains(EVENT_SERVER.toLowerCase()) ? EVENT_DENY_MESSAGE : DENY_MESSAGE;

        if (event.getPreviousServer() != null) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.sendMessage(denyMessage);
            return;
        }

        Optional<RegisteredServer> fallback = proxy.getProxy().getServer(FALLBACK_SERVER);
        if (fallback.isEmpty()) {
            player.disconnect(denyMessage);
            return;
        }

        event.setResult(ServerPreConnectEvent.ServerResult.allowed(fallback.get()));
    }

    public static boolean hasWhitelistAccess(Player player) {
        if (Objects.equals(player.getUniqueId(), Core.DEV_UUID)) return true;
        Map<String, String> wl = Config.getWhitelistedPlayers();
        return wl != null && wl.values().stream().anyMatch(name -> name.equalsIgnoreCase(player.getUsername()));
    }

    public static boolean hasMaintenanceAccess(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION_MAINTENANCE)) return true;
        List<String> maintenanceAccess = Config.getMaintenanceAccess();
        return maintenanceAccess != null && maintenanceAccess.stream().anyMatch(name -> name.equalsIgnoreCase(player.getUsername()));
    }

    public static void sendLimboOnWhitelistRemove(String playerName) {
        Optional<Player> oPlayer = proxy.getProxy().getPlayer(playerName);

        if (oPlayer.isEmpty()) {
            return;
        }

        Player player = oPlayer.get();

        Optional<RegisteredServer> fallback = proxy.getProxy().getServer(FALLBACK_SERVER);
        if (fallback.isEmpty()) {
            Optional<ServerConnection> oCurrentServer = player.getCurrentServer();
            Component denyMessage = oCurrentServer.isPresent() && oCurrentServer.get().getServerInfo().getName().toLowerCase().contains(EVENT_SERVER.toLowerCase()) ? EVENT_DENY_MESSAGE : DENY_MESSAGE;
            player.disconnect(denyMessage);
            return;
        }

        player.createConnectionRequest(fallback.get()).connect();
    }
}
