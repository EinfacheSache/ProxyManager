package de.einfachesache.proxymanager.bungee.listener;

import de.einfachesache.proxymanager.bungee.BungeeProxyManager;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MessageListener implements Listener {

    public static BungeeProxyManager plugin = BungeeProxyManager.getPlugin();
    public static HashMap<ProxiedPlayer, Integer> cooldownCounter = new HashMap<>();
    public static HashMap<ProxiedPlayer, Long> inCooldown = new HashMap<>();
    public static HashMap<ProxiedPlayer, ScheduledTask> taskIDForCooldown = new HashMap<>();

    @EventHandler
    public void OnChat(ChatEvent e) {
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();

        RedisConnector jedis = Core.getRedisConnector();
        if (Boolean.parseBoolean(jedis.get("Commands-Disabled"))) {
            if (e.isCommand()) {
                if (!p.hasPermission("proxy.command.disabled.bypass")) {
                    e.setCancelled(true);
                    BungeeProxyManager.sendMessage(p, "§cAlle Commands sind deaktiviert");
                    return;
                }
            }
        }

        if (Boolean.parseBoolean(jedis.get("Chat-Disabled"))) {
            if (!e.isCommand()) {
                if (!p.hasPermission("proxy.chat.disabled.bypass")) {
                    e.setCancelled(true);
                    BungeeProxyManager.sendMessage(p, "§cDer Chat ist deaktiviert");
                    return;
                }
            }
        }

        if (p.hasPermission("proxy.cooldown.bypass")) return;

        String messages;
        int maxCounter;

        if (e.isCommand()) {
            messages = "§cBitte warte bevor du erneut einen Command sendest";
            maxCounter = 3;
        } else {
            messages = "§cBitte warte bevor du erneut einen Messages sendest";
            maxCounter = 4;
        }

        if (inCooldown.containsKey(p) && ((System.currentTimeMillis() - inCooldown.get(p)) / 1000) <= 5) {
            BungeeProxyManager.sendMessage(p, messages);
            e.setCancelled(true);
            return;
        }

        if (cooldownCounter.containsKey(p)) {
            cooldownCounter.replace(p, cooldownCounter.get(p) + 1);
            if (cooldownCounter.get(p) >= maxCounter) {
                inCooldown.put(p, System.currentTimeMillis());
                cooldownCounter.remove(p);
            }
        } else {
            cooldownCounter.put(p, 1);
        }

        removeInCommandCooldownCounter(p);
    }

    public static void removeInCommandCooldownCounter(ProxiedPlayer p) {
        ScheduledTask taskIDForRemoveCooldown = plugin.getProxy().getScheduler().schedule(BungeeProxyManager.getPlugin(), () -> {
            if (cooldownCounter.containsKey(p)) {
                cooldownCounter.replace(p, cooldownCounter.get(p) - 1);
                if (cooldownCounter.get(p) < 1) {
                    inCooldown.remove(p);
                }
            }else
                stopRemoveCooldownTimer(p);
        }, 1500, TimeUnit.MILLISECONDS);
        taskIDForCooldown.put(p, taskIDForRemoveCooldown);
    }

    public static void stopRemoveCooldownTimer(ProxiedPlayer p) {
        if (!taskIDForCooldown.containsKey(p)) {
            return;
        }
        plugin.getProxy().getScheduler().cancel(taskIDForCooldown.get(p));
        taskIDForCooldown.remove(p);
    }
}

