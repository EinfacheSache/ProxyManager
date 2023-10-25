package de.cubeattack.proxymanager.bungee;

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


public final class ProxyManager extends Plugin {

    private static final PluginManager pm = ProxyServer.getInstance().getPluginManager();
    private final static String PREFIX = "§7[§bNetwork§7] ";
    private final static int ServerID = 15836;
    private static ProxyManager plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {
        Core.run(getLogger());
        new Metrics(this, ServerID);

        if (Config.isManageConnectionEnabled()) pm.registerListener(this, new ManageConnection());
        if (pm.getPlugin("Protocolize") != null) pm.registerCommand(this, new SettingsCMD());

        pm.registerListener(this, new TabCompleteListener());
        pm.registerListener(this, new MessageListener());
        pm.registerListener(this, new ServerKickListener());

        pm.registerCommand(this, new PluginControllerCMD());
        pm.registerCommand(this, new MaintenanceCMD());
        pm.registerCommand(this, new GlobalMuteCMD());
        pm.registerCommand(this, new CommandsCMD());
        pm.registerCommand(this, new ProxyCMD());

        Core.info("Plugin was Enabled successful");
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

    public static net.md_5.bungee.api.plugin.PluginManager getPluginManger() {
        return pm;
    }

    public static void sendMessage(@NotNull CommandSender sender, String msg) {
        sender.sendMessage(new TextComponent(ProxyManager.getPrefix() + msg));
    }

    public static ProxyManager getPlugin() {
        return plugin;
    }


    public static String getPrefix() {
        return PREFIX;
    }
}