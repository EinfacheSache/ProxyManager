package de.einfachesache.proxymanager.bungee.listener;

import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ServerKickListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(ServerKickEvent e) {
        if (!e.getReason().toString().contains("disconnect.spam")
                && !e.getReason().toString().contains("Kicked for spamming")) return;
        e.setCancelled(true);
    }
}
