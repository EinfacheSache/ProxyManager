package de.cubeattack.proxymanager.bungee.listener;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import de.cubeattack.proxymanager.core.Core;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MessageListener implements Listener {

    public static ProxyManager plugin = ProxyManager.getPlugin();
    public static HashMap<ProxiedPlayer, Integer> inCommandCooldownCounter = new HashMap<>();
    public static HashMap<ProxiedPlayer, Integer> inCommandCooldown = new HashMap<>();
    public static HashMap<ProxiedPlayer, ScheduledTask> taskIDForCooldown = new HashMap<>();

    @EventHandler
    public void OnChat(ChatEvent e) {
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();
        if (p.hasPermission("proxymanager.command.bypass.cooldown")) return;

        String messages;
        int counter;
        if (e.isCommand()) {
            messages = "§cBitte warte bevor du erneut einen Command sendest";
            counter = 2;
        } else {
            messages = "§cBitte warte bevor du erneut einen Messages sendest";
            counter = 3;
        }
        if (inCommandCooldown.containsKey(p)) {
            ProxyManager.sendMessage(p, messages);
            e.setCancelled(true);
            return;
        }

        if (inCommandCooldownCounter.containsKey(p)) {
            inCommandCooldownCounter.replace(p, inCommandCooldownCounter.get(p) + 1);
            if (inCommandCooldownCounter.get(p) >= counter) {
                inCommandCooldown.put(p, 5);
                startRemoveCooldownTimer(p);
                inCommandCooldownCounter.remove(p);
            }
        } else {
            inCommandCooldownCounter.put(p, 1);
        }
        removeInCommandCooldownCounter(p);

        if (!Core.getRedisConnector().getJedis().isConnected() || Core.getRedisConnector().getJedis().isBroken()) {
            return;
        }

        if (Boolean.parseBoolean(Core.getRedisConnector().getJedis().get("Commands-Disabled"))) {
            if (e.isCommand()) {
                if (!p.hasPermission("proxymanager.command.bypass.disabled")) {
                    e.setCancelled(true);
                    ProxyManager.sendMessage(p, "§cAlle Commands sind deaktiviert");
                }
            }
        }

        if (Boolean.parseBoolean(Core.getRedisConnector().getJedis().get("Chat-Disabled"))) {
            if (!e.isCommand()) {
                if (!p.hasPermission("proxymanager.chat.bypass.disabled")) {
                    e.setCancelled(true);
                    ProxyManager.sendMessage(p, "§cDer Chat ist deaktiviert");
                }
            }
        }
    }


    public static ScheduledTask taskIDForRemoveCooldown;

    public static void startRemoveCooldownTimer(ProxiedPlayer p) {
        stopRemoveCooldownTimer(p);
        taskIDForRemoveCooldown = plugin.getProxy().getScheduler().schedule(ProxyManager.getPlugin(), () -> {
            if (inCommandCooldown.containsKey(p)) {
                if (inCommandCooldown.get(p) <= 1) {
                    inCommandCooldown.remove(p);
                    stopRemoveCooldownTimer(p);
                } else {
                    int counter = inCommandCooldown.get(p);
                    counter--;
                    inCommandCooldown.replace(p, counter);
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
        taskIDForCooldown.put(p, taskIDForRemoveCooldown);
    }

    public static void stopRemoveCooldownTimer(ProxiedPlayer p) {
        if (!taskIDForCooldown.containsKey(p)) {
            return;
        }
        plugin.getProxy().getScheduler().cancel(taskIDForCooldown.get(p));
        taskIDForCooldown.remove(p);
    }

    public static void removeInCommandCooldownCounter(ProxiedPlayer p) {
        plugin.getProxy().getScheduler().schedule(ProxyManager.getPlugin(), () -> {
            if (inCommandCooldownCounter.containsKey(p)) {
                inCommandCooldownCounter.replace(p, inCommandCooldownCounter.get(p) - 1);
                if (inCommandCooldownCounter.get(p) <= 1) {
                    inCommandCooldown.remove(p);
                }
            }
        }, 1500, TimeUnit.MILLISECONDS);
    }
}

