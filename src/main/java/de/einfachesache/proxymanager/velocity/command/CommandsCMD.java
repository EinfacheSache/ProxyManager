package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandsCMD implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        RedisConnector jedis = Core.getRedisConnector();

        if (args.length == 0) {
            boolean disabled = Boolean.parseBoolean(jedis.get("Commands-Disabled"));
            if (disabled) {
                source.sendMessage(Component.text("§aDu hast alle Commands aktiviert"));
                jedis.set("Commands-Disabled", "false");
            } else {
                source.sendMessage(Component.text("§4Du hast alle Commands deaktiviert"));
                jedis.set("Commands-Disabled", "true");
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                source.sendMessage(Component.text("§aDu hast alle Commands aktiviert"));
                jedis.set("Commands-Disabled", "false");
            } else if (args[0].equalsIgnoreCase("off")) {
                source.sendMessage(Component.text("§4Du hast alle Commands deaktiviert"));
                jedis.set("Commands-Disabled", "true");
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
        return invocation.source().hasPermission("proxy.*");
    }
}
