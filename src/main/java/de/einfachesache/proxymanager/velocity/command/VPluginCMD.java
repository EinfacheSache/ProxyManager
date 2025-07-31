package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.velocity.VelocityProxyManager;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.stream.Collectors;

public class VPluginCMD implements SimpleCommand {

    private final VelocityProxyManager instance;

    public VPluginCMD(VelocityProxyManager instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {
        Collection<PluginContainer> plugins = instance.getProxy().getPluginManager().getPlugins();

        String list = plugins.stream()
                .filter(p -> !p.getDescription().getName().orElse("NULL").equalsIgnoreCase("Velocity"))
                .map(p -> "§a" + p.getDescription().getName().orElse("§cNULL"))
                .collect(Collectors.joining("§f, "));

        String message = "§fPlugins (" + (plugins.size() - 1) + "): " + list;
        invocation.source().sendMessage(Component.text(message));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source() instanceof Player player && player.getUniqueId().equals(Core.DEV_UUID);
    }
}
