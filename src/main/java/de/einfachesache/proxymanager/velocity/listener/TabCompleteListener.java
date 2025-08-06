package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Core;

public class TabCompleteListener {

    @Subscribe
    public void onTabComplete(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        if (!Core.DEV_UUID.equals(player.getUniqueId())) {
            event.getRootNode().removeChildByName("server");
        }
    }
}
