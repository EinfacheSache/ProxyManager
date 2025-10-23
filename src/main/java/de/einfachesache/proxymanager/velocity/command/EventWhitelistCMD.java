package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import de.einfachesache.proxymanager.velocity.listener.LoginAccessControlListener;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventWhitelistCMD implements SimpleCommand {

    public final VProxyManager instance;

    public EventWhitelistCMD(VProxyManager instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            boolean eventWhitelist = Config.isEventWhitelist();
            source.sendMessage(Component.text("§7Die Event-Whitelist ist aktuell §" + (eventWhitelist ? "aaktiviert" : "4deaktiviert")));
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                Config.setEventWhitelist(true);
                instance.getProxy().getAllPlayers().stream()
                        .filter(player -> !LoginAccessControlListener.hasWhitelistAccess(player))
                        .forEach(player -> LoginAccessControlListener.sendLimboOnWhitelistRemove(player.getUsername()));
                source.sendMessage(Component.text("§cDu hast die Event-Whiteliste §aaktiviert"));
                return;
            } else if (args[0].equalsIgnoreCase("off")) {
                Config.setEventWhitelist(false);
                source.sendMessage(Component.text("§cDu hast die Event-Whitelist §4deaktiviert"));
                return;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                boolean isWhitelisted = Config.getWhitelistedPlayers().values().stream().anyMatch(value -> value.equalsIgnoreCase(args[1]));

                if (isWhitelisted) {
                    source.sendMessage(Component.text("§c" + args[1] + " ist bereits auf der Event-Whitelist"));
                    return;
                }

                Config.whitelistPlayer(args[1] + "_by_" + getSourceName(source), args[1]);
                source.sendMessage(Component.text("§cDu hast " + args[1] + " zu der Event-Whitelist hinzugefügt"));
                return;
            }

            if (args[0].equalsIgnoreCase("remove")) {
                boolean wasWhitelisted = Config.removeFromWhitelistByPlayer(args[1]);

                if (!wasWhitelisted) {
                    source.sendMessage(Component.text("§c" + args[1] + " konnte von der Event-Whitelist nicht entfernt"));
                    return;
                }

                LoginAccessControlListener.sendLimboOnWhitelistRemove(args[1]);
                source.sendMessage(Component.text("§cDu hast " + args[1] + " von der Event-Whitelist entfernt"));
                return;
            }
        }

        source.sendMessage(Component.text("§cBitte verwende /maintenance (on/off/add) [player]"));
    }

    public static String getSourceName(CommandSource source) {
        if (source instanceof Player player) {
            return player.getUsername();
        }
        if (source instanceof ConsoleCommandSource) {
            return "CONSOLE";
        }
        return "UNKNOWN";
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            String[] args = invocation.arguments();

            if (args.length > 1) return Collections.emptyList();

            String last = (args.length == 0) ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
            return Stream.of("on", "off", "add", "remove")
                    .filter(s -> s.startsWith(last))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("*");
    }
}
