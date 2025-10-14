package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.velocity.ScreenBuilder;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import de.einfachesache.proxymanager.velocity.listener.LoginAccessControlListener;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaintenanceCMD implements SimpleCommand {

    public final VProxyManager instance;

    public MaintenanceCMD(VProxyManager instance) {
        this.instance = instance;
    }


    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            boolean maintenance = Config.isMaintenanceMode();
            source.sendMessage(Component.text("§7Die Maintenance ist aktuell §" + (maintenance ? "aaktiviert" : "4deaktiviert")));
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                Config.setMaintenanceMode(true);
                instance.getProxy().getAllPlayers().stream()
                        .filter(player -> !LoginAccessControlListener.hasMaintenanceAccess(player))
                        .forEach(player -> player.disconnect(ScreenBuilder.getMaintenanceScreen()));
                source.sendMessage(Component.text("§cDu hast die Maintenance §aaktiviert"));
                return;
            } else if (args[0].equalsIgnoreCase("off")) {
                Config.setMaintenanceMode(false);
                source.sendMessage(Component.text("§cDu hast die Maintenance §4deaktiviert"));
                return;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                Config.addMaintenanceAccess(args[1]);
                source.sendMessage(Component.text("§cDu hast " + args[1] + " zum Maintenance Access hinzugefügt"));
                return;
            }
        }

        source.sendMessage(Component.text("§cBitte verwende /maintenance (on/off/add) [player]"));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            String[] args = invocation.arguments();

            if (args.length > 1) return Collections.emptyList();

            String last = (args.length == 0) ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
            return Stream.of("on", "off", "add")
                    .filter(s -> s.startsWith(last))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.maintenance");
    }
}