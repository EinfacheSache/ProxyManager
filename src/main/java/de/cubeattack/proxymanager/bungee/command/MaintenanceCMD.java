package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.BungeeProxyManager;
import de.cubeattack.proxymanager.core.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class MaintenanceCMD extends Command {

    public MaintenanceCMD() {
        super("maintenance", "*", "wartungsarbeiten");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender == null) return;

        if (args.length == 0) {
            BungeeProxyManager.sendMessage(sender, "§7Die Maintenance ist aktuell §" + (Config.isMaintenanceMode() ? "aaktiviert" : "4deaktivert"));
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                BungeeProxyManager.sendMessage(sender, "§cDu hast den MaintenanceMode §aaktiviert");
                Config.setMaintenanceMode(true);
                return;
            } else if (args[0].equalsIgnoreCase("off")) {
                BungeeProxyManager.sendMessage(sender, "§cDu hast den MaintenanceMode §4deaktiviert");
                Config.setMaintenanceMode(false);
                return;
            }
        }
        BungeeProxyManager.sendMessage(sender, "§cBitte verwende /maintenance (on/off)");
    }
}
