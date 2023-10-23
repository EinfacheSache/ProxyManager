package de.cubeattack.proxymanager.bungee.listener;

import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Arrays;

public class ServerKickListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(ServerKickEvent e) {
        if (!Arrays.toString(e.getKickReasonComponent()).contains("disconnect.spam")
                && !Arrays.toString(e.getKickReasonComponent()).contains("Kicked for spamming")) return;
        e.setCancelled(true);
    }
}
