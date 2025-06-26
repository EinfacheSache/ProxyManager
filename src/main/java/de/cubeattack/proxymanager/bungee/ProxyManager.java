package de.cubeattack.proxymanager.bungee;

import de.cubeattack.api.minecraft.stats.Stats;
import de.cubeattack.api.minecraft.stats.StatsManager;
import de.cubeattack.proxymanager.bungee.command.*;
import de.cubeattack.proxymanager.bungee.listener.ManageConnection;
import de.cubeattack.proxymanager.bungee.listener.MessageListener;
import de.cubeattack.proxymanager.bungee.listener.ServerKickListener;
import de.cubeattack.proxymanager.bungee.listener.TabCompleteListener;
import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class ProxyManager extends Plugin {

    private static final PluginManager pm = ProxyServer.getInstance().getPluginManager();
    private final static String PREFIX = "§7[§bNetwork§7] ";
    private final static int ServerID = 15836;
    private static String statsUUID;
    private static ProxyManager plugin;
    private static Long uptime = 0L;

    @Override
    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {
        Core.run(true, getLogger());
        new Metrics(this, ServerID);
        statsUUID = getPlugin().getProxy().getConfig().getUuid();

        if (Config.isManageConnectionEnabled()) pm.registerListener(this, new ManageConnection());
        if (pm.getPlugin("Protocolize") != null) pm.registerCommand(this, new SettingsCMD());

        StatsManager.runStatsUpdateSchedule(statsUUID, getServerAddress(), getStats(), 5);

        pm.registerListener(this, new TabCompleteListener());
        pm.registerListener(this, new MessageListener());
        pm.registerListener(this, new ServerKickListener());

        pm.registerCommand(this, new PluginControllerCMD());
        pm.registerCommand(this, new MaintenanceCMD());
        pm.registerCommand(this, new GlobalMuteCMD());
        pm.registerCommand(this, new CommandsCMD());
        pm.registerCommand(this, new ProxyCMD());

        Core.info("Plugin was Enabled successful");
        uptime = System.currentTimeMillis();
    }


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
                getProxy().getConfig().getListeners().stream().toList().get(0).isProxyProtocol()
        );
    }

    public void onDisable() {
        for (Plugin pl : pm.getPlugins()) {
            if (pl.getDescription().getName().startsWith("§")) {
                pl.getDescription().setName(pl.getDescription().getName().substring(2));
            }
        }
        Core.shutdown();
        Core.info("Plugin was Disabled successful");
    }

    public static PluginManager getPluginManger() {
        return pm;
    }

    public static void sendMessage(@NotNull CommandSender sender, String msg) {
        sender.sendMessage(new TextComponent(ProxyManager.getPrefix() + msg));
    }

    public static ProxyManager getPlugin() {
        return plugin;
    }

    public static String getServerAddress() {
        return getPlugin().getProxy().getConfig().getListeners().stream().findFirst().orElseThrow().getSocketAddress().toString();
    }

    public static Long getUptime() {
        return uptime;
    }

    public static String getPrefix() {
        return PREFIX;
    }

    public static String getStatsUUID() {
        return statsUUID;
    }
}