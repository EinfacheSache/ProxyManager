package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.command.CommandResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PostCommandInvocationEvent;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProxyShutdownListener {

    private final VProxyManager instance;

    public ProxyShutdownListener(VProxyManager instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onProxyShutdown(PostCommandInvocationEvent event) {

        String cmd = event.getCommand().toLowerCase();
        if (!cmd.equals("end") && !cmd.equals("shutdown")) return;
        if (!(event.getCommandSource() instanceof ConsoleCommandSource) || !event.getResult().equals(CommandResult.EXECUTED)) return;

        Component disconnectMessage = Component.empty()
                .append(Component.text("Der Proxy wird neu gestartet » ", NamedTextColor.RED))
                .append(Component.text("Bitte verbinde dich in wenigen Sekunden erneut", NamedTextColor.GRAY));

        instance.getProxy().getAllPlayers().forEach(player -> player.disconnect(disconnectMessage));
    }
}
