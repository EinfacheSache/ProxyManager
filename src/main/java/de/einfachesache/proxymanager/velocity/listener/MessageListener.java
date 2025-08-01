package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import de.einfachesache.proxymanager.velocity.VelocityProxyManager;
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
        Player player = event.getPlayer();

        RedisConnector jedis = Core.getRedisConnector();
        if (Boolean.parseBoolean(jedis.get("Chat-Disabled"))) {
            if (!player.hasPermission("proxy.chat.disabled.bypass") && !player.getUniqueId().equals(Core.DEV_UUID)) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
                player.sendMessage(Component.text(VelocityProxyManager.PREFIX + "§cDer Chat ist deaktiviert"));
                return;
            }
        }

        if (player.hasPermission("proxy.cooldown.bypass") || player.getUniqueId().equals(Core.DEV_UUID)) {
            return;
        }


        long now = System.currentTimeMillis();
        if (inCooldown.containsKey(player) && (now - inCooldown.get(player)) / 1000 <= 5) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            player.sendMessage(Component.text(VelocityProxyManager.PREFIX + "§cBitte warte bevor du erneut eine Nachricht sendest"));
            return;
        }

        cooldownCounter.merge(player, 1, Integer::sum);
        if (cooldownCounter.get(player) >= 4) {
            inCooldown.put(player, now);
            cooldownCounter.remove(player);
        }

        scheduleCooldownReduction(player);
    }

    private void scheduleCooldownReduction(Player p) {
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
