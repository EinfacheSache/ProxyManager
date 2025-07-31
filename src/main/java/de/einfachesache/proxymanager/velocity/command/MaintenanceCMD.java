package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MaintenanceCMD implements SimpleCommand {

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
                source.sendMessage(Component.text("§cDu hast den MaintenanceMode §aaktiviert"));
                return;
            } else if (args[0].equalsIgnoreCase("off")) {
                Config.setMaintenanceMode(false);
                source.sendMessage(Component.text("§cDu hast den MaintenanceMode §4deaktiviert"));
                return;
            }
        }

        source.sendMessage(Component.text("§cBitte verwende /maintenance (on/off)"));
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