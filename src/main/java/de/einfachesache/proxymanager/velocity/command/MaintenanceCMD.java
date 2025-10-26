package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.velocity.ScreenBuilder;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import de.einfachesache.proxymanager.velocity.listener.LoginAccessControlListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MaintenanceCMD implements SimpleCommand {

    private final List<String> subCommands = Arrays.asList("on", "off", "list", "add", "remove");

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
            }

            if (args[0].equalsIgnoreCase("off")) {
                Config.setMaintenanceMode(false);
                source.sendMessage(Component.text("§cDu hast die Maintenance §4deaktiviert"));
                return;
            }

            if (args[0].equalsIgnoreCase("list")) {
                source.sendMessage(Component.text("Maintenance Accessed Player", NamedTextColor.YELLOW));
                Config.getMaintenanceAccess().forEach(access -> source.sendMessage(Component.text("- " + access, NamedTextColor.YELLOW)));
                return;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                Config.addMaintenanceAccess(args[1]);
                source.sendMessage(Component.text("Du hast " + args[1] + " zum Maintenance Access hinzugefügt", NamedTextColor.YELLOW));
                return;
            }

            if (args[0].equalsIgnoreCase("remove")) {
                Config.removeMaintenanceAccess(args[1]);
                source.sendMessage(Component.text("§cDu hast " + args[1] + " vom Maintenance Access entfernt", NamedTextColor.RED));
                return;
            }
        }

        source.sendMessage(Component.text("§cBitte verwende /maintenance (on/off/add/remove) [player]"));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            String[] arguments = invocation.arguments();

            if (arguments.length == 0) {
                return subCommands;
            }

            if (arguments.length == 1) {
                String prefix = arguments[0].toLowerCase(Locale.ROOT);
                return subCommands.stream()
                        .filter(cmd -> cmd.toLowerCase(Locale.ROOT).startsWith(prefix))
                        .collect(Collectors.toList());
            }

            if (arguments.length == 2 && "remove".equalsIgnoreCase(arguments[0])) {
                String playerPrefix = arguments[1].toLowerCase(Locale.ROOT);

                return Config.getMaintenanceAccess().stream()
                        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(playerPrefix))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.maintenance");
    }
}