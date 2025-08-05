package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Core;

public class TabCompleteListener {

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        Player player = event.getPlayer();
        if (!Core.DEV_UUID.equals(player.getUniqueId())) {
            event.getSuggestions().removeIf(s -> s.toLowerCase().startsWith("server"));
        }
    }
}
