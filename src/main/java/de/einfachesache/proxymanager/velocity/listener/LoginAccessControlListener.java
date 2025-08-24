package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.velocity.ScreenBuilder;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoginAccessControlListener {

    private static final String EVENT_SERVER = "Event";
    private static final String FALLBACK_SERVER = "Limbo";
    private static final String BYPASS_PERMISSION = "proxy.join.bypass";
    private static final Component DENY_MESSAGE = Component.text("⛔ Du bist fürs Event nicht whitelisted » /whitelist im Discord", NamedTextColor.RED);

    private final VProxyManager proxy;

    public LoginAccessControlListener(VProxyManager proxy) {
        this.proxy = proxy;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        if (!Config.isMaintenanceMode() || hasMaintenanceAccess(player)) {
            return;
        }

        player.disconnect(ScreenBuilder.getMaintenanceScreen());
    }

    @Subscribe
    public void onPreConnect(ServerPreConnectEvent event) {
        if (!Config.isEventWhitelist()) return;
        if (!event.getOriginalServer().getServerInfo().getName().equalsIgnoreCase(EVENT_SERVER)) return;

        var player = event.getPlayer();
        if (hasWhitelistAccess(player)) return;

        if (event.getPreviousServer() == null) {
            Optional<RegisteredServer> fallback = proxy.getProxy().getServer(FALLBACK_SERVER);
            if (fallback.isEmpty()) {
                player.disconnect(DENY_MESSAGE);
                return;
            }
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(fallback.get()));
        } else {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
        player.sendMessage(DENY_MESSAGE);
    }

    private boolean hasWhitelistAccess(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION)) return true;
        Map<String, String> wl = Config.getWhitelistedPlayers();
        return wl != null && wl.values().stream().anyMatch(name -> name.equalsIgnoreCase(player.getUsername()));
    }

    public static boolean hasMaintenanceAccess(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION)) return true;
        List<String> maintenanceAccess = Config.getMaintenanceAccess();
        return maintenanceAccess != null && maintenanceAccess.stream().anyMatch(name -> name.equalsIgnoreCase(player.getUsername()));
    }
}
