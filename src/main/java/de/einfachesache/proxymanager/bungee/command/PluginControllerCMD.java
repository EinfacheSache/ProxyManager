package de.einfachesache.proxymanager.bungee.command;

import de.einfachesache.proxymanager.bungee.BungeeProxyManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class PluginControllerCMD extends Command {

    public PluginControllerCMD() {
        super("bungeeplugins", "proxymanager.*", "bpl");
    }

    private static final ArrayList<Plugin> disabledPluginList = new ArrayList<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender == null) return;

        String pluginName = null;
        Plugin plugin = null;
        if (args.length >= 2) {
            pluginName = args[1];
            plugin = BungeeProxyManager.getPluginManger().getPlugin(pluginName);
        }

        try {
            switch (args[0]) {
                case "list" -> list(sender);
                case "disable" -> disable(plugin, pluginName, sender);
                case "enable" -> enable(plugin, pluginName, sender);
                case "restart" -> restart(plugin, pluginName, sender);
                case "rename" -> rename(plugin, pluginName, args[2], sender);
                default -> showHelp(sender);
            }
            return;
        } catch (Exception ignored) {
        }
        showHelp(sender);
    }

    public static void list(CommandSender sender) {

        Collection<Plugin> plugins = new ArrayList<>(BungeeProxyManager.getPluginManger().getPlugins());
        StringBuilder pl = new StringBuilder();
        AtomicInteger i = new AtomicInteger();

        plugins.forEach(plugin -> {
            String name = plugin.getDescription().getName();
            if (!name.startsWith("cmd_") && !name.startsWith("reconnect_yaml")) {
                pl.append(plugin.getDescription().getName()).append("§f, §a");
            } else {
                i.getAndIncrement();
            }
        });

        BungeeProxyManager.sendMessage(sender, "§fPlugins (" + (plugins.size() - i.get()) + "): §a" +
                pl.substring(0, pl.length() - 4));
    }

    public static void enable(Plugin plugin, String pluginName, CommandSender sender) {
        try {
            if (pluginName.startsWith("§")) {
                pluginName = pluginName.substring(2);
            }

            plugin.getDescription().setName(pluginName);
            plugin.onLoad();
            plugin.onEnable();
            plugin.getDescription().setName(ChatColor.GREEN + pluginName);

            disabledPluginList.remove(plugin);

            BungeeProxyManager.sendMessage(sender, "§cDas Plugin §6" + pluginName + "§c wurde§6 erfolgreich §aEnabled.");
        } catch (NullPointerException e) {
            BungeeProxyManager.sendMessage(sender, "§cDas Plugin §6" + pluginName + "§c gibt es nicht.");
        }
    }

    public static void disable(Plugin plugin, String pluginName, CommandSender sender) {
        try {

            BungeeProxyManager.getPluginManger().unregisterListeners(plugin);
            BungeeProxyManager.getPluginManger().unregisterCommands(plugin);

            if (pluginName.startsWith("§")) {
                pluginName = pluginName.substring(2);
            }

            plugin.getDescription().setName(pluginName);
            plugin.onDisable();
            plugin.getDescription().setName(ChatColor.DARK_RED + pluginName);

            disabledPluginList.add(plugin);

            BungeeProxyManager.sendMessage(sender, "§cDas Plugin §6" + pluginName + "§c wurde§6 erfolgreich §4Disabled.");
        } catch (NullPointerException e) {
            BungeeProxyManager.sendMessage(sender, "§cDas Plugin §6" + pluginName + "§c gibt es nicht.");
        }
    }

    public static void restart(Plugin plugin, String pluginName, CommandSender sender) {
        disable(plugin, pluginName, sender);
        enable(plugin, pluginName, sender);
    }

    public static void rename(Plugin plugin, String pluginName, String newPluginName, CommandSender sender) {
        try {
            plugin.getDescription().setName(newPluginName);
            BungeeProxyManager.sendMessage(sender, "§cDas Plugin §6" + pluginName + "§c wurde§6 erfolgreich §czu §6" + newPluginName + " §eRenamed.");
        } catch (NullPointerException e) {
            BungeeProxyManager.sendMessage(sender, "§cDas Plugin §6" + pluginName + "§c gibt es nicht.");
        }
    }

    public static void showHelp(CommandSender sender) {
        BungeeProxyManager.sendMessage(sender, "§c--------Help--------");
        BungeeProxyManager.sendMessage(sender, "§c/bpl list");
        BungeeProxyManager.sendMessage(sender, "§c/bpl enable (Plugin)");
        BungeeProxyManager.sendMessage(sender, "§c/bpl disable (Plugin)");
        BungeeProxyManager.sendMessage(sender, "§c/bpl restart (Plugin)");
        BungeeProxyManager.sendMessage(sender, "§c/bpl rename (Plugin) (Name)");
    }

    public static ArrayList<Plugin> getDisabledPluginList() {
        return disabledPluginList;
    }
}
