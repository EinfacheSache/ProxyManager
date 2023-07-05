package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class PluginController extends Command {

    public PluginController() {
        super("bpl");
    }

    private static final ArrayList<Plugin> disabledPluginList = new ArrayList<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender == null)return;

        if(!sender.hasPermission("proxymanager.bpl")) {
           ProxyManager.sendMessage(sender, "§§Du hast dazu keine Rechte");
           return;
        }

        String pluginName = null;
        Plugin plugin = null;
        if(args.length >= 2){
            pluginName = args[1];
            plugin = ProxyManager.getPluginManger().getPlugin(pluginName);
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
        }catch (Exception ignored){}
        showHelp(sender);
    }

    public static void list(CommandSender sender){

        Collection<Plugin> plugins = new ArrayList<>(ProxyManager.getPluginManger().getPlugins());
        StringBuilder pl = new StringBuilder();
        AtomicInteger i = new AtomicInteger();

        plugins.forEach(plugin -> {
            String name = plugin.getDescription().getName();
            if(!name.startsWith("cmd_") && !name.startsWith("reconnect_yaml")){
                pl.append(plugin.getDescription().getName()).append("§f, §a");
            }else{
                i.getAndIncrement();
            }
        });

        ProxyManager.sendMessage(sender,  "§fPlugins (" + (plugins.size() - i.get()) + "): §a" +
                pl.substring(0, pl.length() - 4));
    }

    public static void enable(Plugin plugin, String pluginName, CommandSender sender){
        try {
            if(pluginName.startsWith("§")){
                pluginName = pluginName.substring(2);
            }

            plugin.getDescription().setName(pluginName);
            plugin.onLoad();
            plugin.onEnable();
            plugin.getDescription().setName(ChatColor.GREEN + pluginName);

            disabledPluginList.remove(plugin);

            ProxyManager.sendMessage(sender,"§cDas Plugin §6" + pluginName + "§c wurde§6 erfolgreich §aEnabled.");
        }catch (NullPointerException e){
            ProxyManager.sendMessage(sender,"§cDas Plugin §6" + pluginName + "§c gibt es nicht.");
        }
    }

    public static void disable(Plugin plugin, String pluginName, CommandSender sender){
        try {

            ProxyManager.getPluginManger().unregisterListeners(plugin);
            ProxyManager.getPluginManger().unregisterCommands(plugin);

            if(pluginName.startsWith("§")){
                pluginName = pluginName.substring(2);
            }

            plugin.getDescription().setName(pluginName);
            plugin.onDisable();
            plugin.getDescription().setName(ChatColor.DARK_RED + pluginName);

            disabledPluginList.add(plugin);

            ProxyManager.sendMessage(sender,"§cDas Plugin §6" + pluginName + "§c wurde§6 erfolgreich §4Disabled.");
        }catch (NullPointerException e){
            ProxyManager.sendMessage(sender,"§cDas Plugin §6" + pluginName + "§c gibt es nicht.");
        }
    }

    public static void restart(Plugin plugin, String pluginName, CommandSender sender){
        disable(plugin, pluginName, sender);
        enable(plugin, pluginName, sender);
    }

    public static void rename(Plugin plugin, String pluginName, String newPluginName, CommandSender sender){
        try {
            plugin.getDescription().setName(newPluginName);
            ProxyManager.sendMessage(sender,"§cDas Plugin §6" + pluginName + "§c wurde§6 erfolgreich §czu §6" + newPluginName + " §eRenamed.");
        } catch (NullPointerException e) {
            ProxyManager.sendMessage(sender,"§cDas Plugin §6" + pluginName + "§c gibt es nicht.");
        }
    }

    public static void showHelp(CommandSender sender){
        ProxyManager.sendMessage(sender,"§c--------Help--------");
        ProxyManager.sendMessage(sender,"§c/bpl list");
        ProxyManager.sendMessage(sender,"§c/bpl enable (Plugin)");
        ProxyManager.sendMessage(sender,"§c/bpl disable (Plugin)");
        ProxyManager.sendMessage(sender,"§c/bpl restart (Plugin)");
        ProxyManager.sendMessage(sender,"§c/bpl rename (Plugin) (Name)");
    }

    public static ArrayList<Plugin> getDisabledPluginList() {
        return disabledPluginList;
    }
}
