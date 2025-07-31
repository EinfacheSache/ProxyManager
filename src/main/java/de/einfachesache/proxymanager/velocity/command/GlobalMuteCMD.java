package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GlobalMuteCMD implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        RedisConnector jedis = Core.getRedisConnector();

        if (args.length == 0) {
            boolean chatDisabled = Boolean.parseBoolean(jedis.get("Chat-Disabled"));
            if (chatDisabled) {
                source.sendMessage(Component.text("§aDu hast den Chat aktiviert"));
                jedis.set("Chat-Disabled", "false");
            } else {
                source.sendMessage(Component.text("§4Du hast den Chat deaktiviert"));
                jedis.set("Chat-Disabled", "true");
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("off")) {
                source.sendMessage(Component.text("§aDu hast den Chat aktiviert"));
                jedis.set("Chat-Disabled", "false");
            } else if (args[0].equalsIgnoreCase("on")) {
                source.sendMessage(Component.text("§4Du hast den Chat deaktiviert"));
                jedis.set("Chat-Disabled", "true");
            }
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> list = new ArrayList<>();
            String[] args = invocation.arguments();

            list.add("on");
            list.add("off");

            for (String tab : list) {
                if (args.length == 0) continue;
                if (!tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    list.remove(tab);
                }
            }

            return list;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.*") || (invocation.source() instanceof Player player && player.getUniqueId().equals(Core.DEV_UUID));
    }
}