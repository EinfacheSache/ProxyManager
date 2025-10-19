package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.util.ServerLink;
import de.einfachesache.proxymanager.core.Config;
import net.kyori.adventure.text.Component;

import java.util.List;

public class JoinServerLinkListener {

    private static final List<ServerLink> serverLinks = List.of(
            ServerLink.serverLink(Component.text("Discord"), Config.getDiscordLink()),
            ServerLink.serverLink(Component.text("Status"), "https://status.flareonevents.de"),
            ServerLink.serverLink(Component.text("Map"), "https://map.flareonevents.de")
    );

    @Subscribe()
    public void onLogin(PostLoginEvent event) {
        event.getPlayer().setServerLinks(serverLinks);
    }
}

