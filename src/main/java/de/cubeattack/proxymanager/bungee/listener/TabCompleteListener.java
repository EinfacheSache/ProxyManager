package de.cubeattack.proxymanager.bungee.listener;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import de.cubeattack.proxymanager.bungee.command.PluginControllerCMD;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.Collections;

public class TabCompleteListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onTabComplete(TabCompleteEvent event) {

        String cursor = event.getCursor().toLowerCase();
        ArrayList<String> commands = new ArrayList<>();
        ArrayList<String> tablistOne = new ArrayList<>();
        ArrayList<String> tablistTwo = new ArrayList<>();
        String[] cursorSplit = cursor.split(" ");

        if (cursor.startsWith("/bpl")) {

            if (event.getSender() instanceof ProxiedPlayer && !((ProxiedPlayer) event.getSender()).hasPermission("proxymanager.bpl"))
                return;

            for (Plugin plugin : ProxyManager.getPluginManger().getPlugins()) {
                String plName = plugin.getDescription().getName();

                if (plName.equals("cmd_find") || plName.equals("cmd_server") || plName.equals("cmd_alert") || plName.equals("cmd_send") || plName.equals("cmd_list") || plName.equals("reconnect_yaml")) {
                    continue;
                }

                if (plName.startsWith("§")) {
                    plName = plName.substring(2);
                }

                if (cursorSplit.length >= 2 && !cursorSplit[1].equalsIgnoreCase("list")) {

                    if ("enable" .startsWith(cursorSplit[1]) && !PluginControllerCMD.getDisabledPluginList().contains(plugin)) {
                        continue;
                    }

                    if ("disable" .startsWith(cursorSplit[1]) && PluginControllerCMD.getDisabledPluginList().contains(plugin)) {
                        continue;
                    }

                    tablistTwo.add(plName);
                }
            }

            commands.add("/bpl");
            tablistOne.add("list");
            tablistOne.add("enable");
            tablistOne.add("disable");
            tablistOne.add("restart");
            tablistOne.add("rename");

            event.getSuggestions().addAll(completor(true, cursor, commands, tablistOne, tablistTwo));

        } else {
            if (event.getSender() instanceof ProxiedPlayer && (!((ProxiedPlayer) event.getSender()).hasPermission("proxymanager.chat")) && (!((ProxiedPlayer) event.getSender()).hasPermission("proxymanager.command")))
                return;

            commands.add("/gmute");
            commands.add("/commands");
            tablistOne.add("on");
            tablistOne.add("off");

            event.getSuggestions().addAll(completor(true, cursor, commands, tablistOne));

        }
    }


    @SafeVarargs
    public static ArrayList<String> completor(boolean sort, String cursor, ArrayList<String> cmds, ArrayList<String>... tabLists) {

        ArrayList<String> completions = new ArrayList<>();

        int pos = 0;
        String[] splitCursor = cursor.split(" ");

        for (ArrayList<String> tabList : tabLists) {
            pos++;

            if (sort) {
                Collections.sort(cmds);
                Collections.sort(tabList);
            }

            for (String cmd : cmds) {

                if (!cursor.startsWith(cmd + " ")) {
                    continue;
                }

                if (splitCursor.length == pos + 1) {
                    String current = splitCursor[splitCursor.length - 1];
                    for (String tab : tabList) {
                        if (tab.toLowerCase().startsWith(current) && !cursor.endsWith(" ")) {
                            completions.add(tab);
                        }
                    }
                } else if (cursor.endsWith(" ") && splitCursor.length == (pos)) {
                    if (pos >= 2 && !tabLists[pos - 2].contains(splitCursor[pos - 1])) {
                        continue;
                    }
                    completions.addAll(tabList);
                }
            }
        }
        return completions;
    }
}