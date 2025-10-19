package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProxyShutdownListener {

    private final VProxyManager instance;

    public ProxyShutdownListener(VProxyManager instance) {
        this.instance = instance;
    }

    @Subscribe(priority = Short.MAX_VALUE)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        Component disconnectMessage = Component.empty()
                .append(Component.text("Der Proxy wird neu gestartet", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.text("Bitte verbinde dich in wenigen Sekunden erneut", NamedTextColor.GRAY));

        instance.getProxy().getAllPlayers().forEach(player -> player.disconnect(disconnectMessage));
    }
}
