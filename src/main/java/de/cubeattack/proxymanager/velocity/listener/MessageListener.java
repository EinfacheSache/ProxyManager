package de.cubeattack.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.cubeattack.proxymanager.velocity.VelocityProxyManager;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.core.RedisConnector;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class MessageListener {

    private final VelocityProxyManager proxy;
    private static final Map<Player, Integer> cooldownCounter = new ConcurrentHashMap<>();
    private static final Map<Player, Long> inCooldown = new ConcurrentHashMap<>();
    private static final Map<Player, ScheduledTask> taskIDForCooldown = new ConcurrentHashMap<>();

    public MessageListener(VelocityProxyManager proxy) {
        this.proxy = proxy;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player p = event.getPlayer();
        String msg = event.getMessage();

        // Redis und Core nutzen
        RedisConnector jedis = Core.getRedisConnector();
        if (Boolean.parseBoolean(jedis.get("Commands-Disabled"))) {
            if (msg.startsWith("/")) {
                if (!p.hasPermission("proxy.command.disabled.bypass")) {
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    p.sendMessage(Component.text(VelocityProxyManager.PREFIX + "§cAlle Commands sind deaktiviert"));
                    return;
                }
            }
        }

        if (Boolean.parseBoolean(jedis.get("Chat-Disabled"))) {
            if (!msg.startsWith("/")) {
                if (!p.hasPermission("proxy.chat.disabled.bypass")) {
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    p.sendMessage(Component.text(VelocityProxyManager.PREFIX + "§cDer Chat ist deaktiviert"));
                    return;
                }
            }
        }

        if (p.hasPermission("proxy.cooldown.bypass")) {
            return;
        }

        String cooldownMsg;
        int maxCount;
        if (msg.startsWith("/")) {
            cooldownMsg = "§cBitte warte bevor du erneut einen Command sendest";
            maxCount = 3;
        } else {
            cooldownMsg = "§cBitte warte bevor du erneut eine Nachricht sendest";
            maxCount = 4;
        }

        long now = System.currentTimeMillis();
        if (inCooldown.containsKey(p) && (now - inCooldown.get(p)) / 1000 <= 5) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            p.sendMessage(Component.text(VelocityProxyManager.PREFIX + cooldownMsg));
            return;
        }

        cooldownCounter.merge(p, 1, Integer::sum);
        if (cooldownCounter.get(p) >= maxCount) {
            inCooldown.put(p, now);
            cooldownCounter.remove(p);
        }

        scheduleCooldownReduction(p);
    }

    private void scheduleCooldownReduction(Player p) {
        // Vorherigen Task entfernen, falls vorhanden
        stopCooldownTask(p);
        ScheduledTask task = proxy.getProxy().getScheduler()
                .buildTask(proxy, () -> {
                    cooldownCounter.computeIfPresent(p, (player, count) -> count > 1 ? count - 1 : null);
                    if (!cooldownCounter.containsKey(p)) {
                        inCooldown.remove(p);
                    }
                })
                .delay(Duration.ofMillis(1500))
                .schedule();
        taskIDForCooldown.put(p, task);
    }

    private void stopCooldownTask(Player p) {
        ScheduledTask task = taskIDForCooldown.remove(p);
        if (task != null) {
            task.cancel();
        }
    }
}
