package de.einfachesache.proxymanager.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.einfachesache.api.minecraft.metric.StatsContainer;
import de.einfachesache.api.minecraft.metric.StatsManager;
import de.einfachesache.api.minecraft.metric.StatsProvider;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.velocity.command.*;
import de.einfachesache.proxymanager.velocity.listener.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VProxyManager implements ProxyInstance, StatsProvider {

    private final ProxyServer proxy;
    private final Logger logger;

    public static final String PREFIX = "§7[§bNetwork§7] ";

    @Inject
    public VProxyManager(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {

        Core.run(this, logger);

        register();

        StatsManager.runStatsUpdateSchedule(String.valueOf(UUID.randomUUID()), proxy.getBoundAddress().getHostName(), this, 5);

        Core.info("Proxy-Manager was successfully enabled");
    }


    private void register() {
        EventManager em = proxy.getEventManager();
        CommandManager cm = proxy.getCommandManager();

        cm.register(cm.metaBuilder("proxy").aliases("pr", "proxygui").build(), new ProxyCMD(this));
        cm.register(cm.metaBuilder("maintenance").build(), new MaintenanceCMD(this));
        cm.register(cm.metaBuilder("settings").build(), new SettingsCMD(this));
        cm.register(cm.metaBuilder("commands").build(), new CommandsCMD());
        cm.register(cm.metaBuilder("gmute").build(), new GlobalMuteCMD());

        em.register(this, new MessageListener(this));
        em.register(this, new CommandListener(this));
        em.register(this, new WhitelistListener(this));
        em.register(this, new TabCompleteListener());
        em.register(this, new VPermissionProvider());

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
    public StatsContainer getStats() {
        return new StatsContainer(
                "velocity",
                proxy.getVersion().getVersion(),
                proxy.getVersion().getName(),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                VProxyManager.class.getPackage().getImplementationVersion(),
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