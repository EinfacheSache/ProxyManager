package de.einfachesache.proxymanager.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.cubeattack.api.minecraft.stats.Stats;
import de.cubeattack.api.minecraft.stats.StatsManager;
import de.cubeattack.api.minecraft.stats.StatsProvider;
import de.einfachesache.proxymanager.ProxyInstance;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.velocity.command.*;
import de.einfachesache.proxymanager.velocity.listener.CommandListener;
import de.einfachesache.proxymanager.velocity.listener.ConnectionListener;
import de.einfachesache.proxymanager.velocity.listener.MessageListener;
import de.einfachesache.proxymanager.velocity.listener.TabCompleteListener;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(
        id = "proxymanger",
        name = "ProxyManager",
        version = "${project.version}:${buildNumber}",
        url = "https://einfachesache.de/discord",
        description = "Manage your ProxyServer",
        authors = {"EinfacheSache"},
        dependencies = {
                @Dependency(id = "protocolize", optional = true)
        }
)
public class VelocityProxyManager implements ProxyInstance, StatsProvider {

    private static ProxyServer proxy;
    private static Logger logger;

    public static final String PREFIX = "§7[§bNetwork§7] ";

    @Inject
    public VelocityProxyManager(ProxyServer proxy, Logger logger) {
        VelocityProxyManager.proxy = proxy;
        VelocityProxyManager.logger = logger;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {

        Core.run(this, logger);

        register();

        StatsManager.runStatsUpdateSchedule(String.valueOf(UUID.randomUUID()), proxy.getBoundAddress().getHostString(), this, 5);

        Core.UPTIME = System.currentTimeMillis();

        Core.info("Proxy-Manager was successfully enabled");
    }


    private void register() {
        EventManager em = proxy.getEventManager();
        CommandManager cm = proxy.getCommandManager();

        cm.register(cm.metaBuilder("proxy").aliases("pr", "proxygui").build(), new ProxyCMD(this));
        cm.register(cm.metaBuilder("velocityplugins").aliases("vpl").build(), new VPluginCMD(this));
        cm.register(cm.metaBuilder("commands").aliases("cmd").build(), new CommandsCMD());
        cm.register(cm.metaBuilder("settings").build(), new SettingsCMD(this));
        cm.register(cm.metaBuilder("maintenance").build(), new MaintenanceCMD());
        cm.register(cm.metaBuilder("gmute").build(), new GlobalMuteCMD());

        em.register(this, new MessageListener(this));
        em.register(this, new CommandListener(this));
        em.register(this, new TabCompleteListener());

        if (Config.isManageConnectionEnabled()) {
            em.register(this, new ConnectionListener());
        }
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        Core.shutdown();
        Core.info("Proxy-Manager was successfully disabled");
    }


    public void sendMessage(Player player, String msg) {
        player.sendMessage(Component.text(PREFIX + msg));
    }


    @NotNull
    public Stats getStats() {
        return new Stats(
                "velocity",
                proxy.getVersion().getVersion(),
                proxy.getVersion().getName(),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                VelocityProxyManager.class.getPackage().getImplementationVersion(),
                null,
                null,
                null,
                null,
                proxy.getPlayerCount(),
                proxy.getAllServers().size(),
                Runtime.getRuntime().availableProcessors(),
                proxy.getConfiguration().isOnlineMode(),
                false
        );
    }

    public ProxyServer getProxy() {
        return proxy;
    }


    @Override
    public int getOnlinePlayerCount() {
        return proxy.getAllPlayers().size();
    }

    @Override
    public int getPlayerLimit() {
        return proxy.getConfiguration().getShowMaxPlayers();
    }

    @Override
    public List<BackendServer> getBackendServerAsString() {
        return proxy.getAllServers().stream()
                .map(registeredServer -> new BackendServer(
                        registeredServer.getServerInfo().getName(),
                        registeredServer.getPlayersConnected().size()
                ))
                .collect(Collectors.toList());
    }
}