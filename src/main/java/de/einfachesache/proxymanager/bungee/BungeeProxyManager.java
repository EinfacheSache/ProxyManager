package de.einfachesache.proxymanager.bungee;

import de.cubeattack.api.minecraft.stats.Stats;
import de.cubeattack.api.minecraft.stats.StatsManager;
import de.cubeattack.api.minecraft.stats.StatsProvider;
import de.einfachesache.proxymanager.ProxyInstance;
import de.einfachesache.proxymanager.bungee.command.*;
import de.einfachesache.proxymanager.bungee.listener.ManageConnection;
import de.einfachesache.proxymanager.bungee.listener.MessageListener;
import de.einfachesache.proxymanager.bungee.listener.ServerKickListener;
import de.einfachesache.proxymanager.bungee.listener.TabCompleteListener;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class BungeeProxyManager extends Plugin implements ProxyInstance, StatsProvider {

    private static final PluginManager pm = ProxyServer.getInstance().getPluginManager();
    private final static String PREFIX = "§7[§bNetwork§7] ";
    private static BungeeProxyManager plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {
        Core.run(this, getLogger());

        if (Config.isManageConnectionEnabled()) pm.registerListener(this, new ManageConnection());
        if (pm.getPlugin("Protocolize") != null) pm.registerCommand(this, new SettingsCMD());

        StatsManager.runStatsUpdateSchedule(getPlugin().getProxy().getConfig().getUuid(), getServerAddress(), this, 5);

        pm.registerListener(this, new TabCompleteListener());
        pm.registerListener(this, new MessageListener());
        pm.registerListener(this, new ServerKickListener());

        pm.registerCommand(this, new PluginControllerCMD());
        pm.registerCommand(this, new MaintenanceCMD());
        pm.registerCommand(this, new GlobalMuteCMD());
        pm.registerCommand(this, new CommandsCMD());
        pm.registerCommand(this, new ProxyCMD());


        Core.UPTIME = System.currentTimeMillis();
        Core.info("Plugin was successfully enabled");
    }

    public void onDisable() {
        for (Plugin pl : pm.getPlugins()) {
            if (pl.getDescription().getName().startsWith("§")) {
                pl.getDescription().setName(pl.getDescription().getName().substring(2));
            }
        }
        Core.shutdown();
        Core.info("Plugin was successfully disabled");
    }

    public static void sendMessage(@NotNull CommandSender sender, String msg) {
        sender.sendMessage(new TextComponent(PREFIX + msg));
    }

    public static String getServerAddress() {
        return getPlugin().getProxy().getConfig().getListeners().stream().findFirst().orElseThrow().getSocketAddress().toString();
    }

    public static PluginManager getPluginManger() {
        return pm;
    }

    public static BungeeProxyManager getPlugin() {
        return plugin;
    }

    @NotNull
    public Stats getStats() {
        return new Stats(
                "bungeecord",
                getProxy().getVersion(),
                getProxy().getName(),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                getDescription().getVersion(),
                null,
                null,
                null,
                null,
                getProxy().getOnlineCount(),
                getProxy().getServers().size(),
                Runtime.getRuntime().availableProcessors(),
                getProxy().getConfig().isOnlineMode(),
                getProxy().getConfig().getListeners().stream().toList().getFirst().isProxyProtocol()
        );
    }


    @Override
    public int getOnlinePlayerCount() {
        return getProxy().getPlayers().size();
    }

    @Override
    public int getPlayerLimit() {
        return getProxy().getConfig().getPlayerLimit();
    }

    @Override
    public List<BackendServer> getBackendServerAsString() {
        return ProxyServer.getInstance().getServers().values().stream()
                .map(serverInfo -> new BackendServer(serverInfo.getName(), serverInfo.getPlayers().size()))
                .collect(Collectors.toList());
    }
}