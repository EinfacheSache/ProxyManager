package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;

public class TabCompleteListener {

    @Subscribe
    public void onTabComplete(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("velocity.command.server")) {
            event.getRootNode().removeChildByName("server");
        }
    }
}
