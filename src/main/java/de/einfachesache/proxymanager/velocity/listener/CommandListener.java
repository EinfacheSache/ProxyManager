package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandListener {

    private final VProxyManager proxy;
    private final Map<Player, Integer> cooldownCounter = new ConcurrentHashMap<>();
    private final Map<Player, Long> inCooldown = new ConcurrentHashMap<>();
    private final Map<Player, ScheduledTask> taskIDForCooldown = new ConcurrentHashMap<>();

    public CommandListener(VProxyManager proxy) {
        this.proxy = proxy;
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player player))
            return;

        RedisConnector jedis = Core.getRedisConnector();
        if (Boolean.parseBoolean(jedis.get("Commands-Disabled"))) {
            if (!player.hasPermission("proxy.command.disabled.bypass")) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
                player.sendMessage(Component.text(VProxyManager.PREFIX + "§cAlle Commands sind deaktiviert"));
                return;
            }
        }

        if (event.getCommand().toLowerCase().startsWith("server") && !Core.DEV_UUID.equals(player.getUniqueId())) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }

        if (player.hasPermission("proxy.cooldown.bypass")) {
            return;
        }

        long now = System.currentTimeMillis();
        if (inCooldown.containsKey(player) && (now - inCooldown.get(player)) / 1000 <= 5) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            player.sendMessage(Component.text(VProxyManager.PREFIX + "§cBitte warte bevor du erneut einen Command sendest"));
            return;
        }

        cooldownCounter.merge(player, 1, Integer::sum);
        if (cooldownCounter.get(player) >= 3) {
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
