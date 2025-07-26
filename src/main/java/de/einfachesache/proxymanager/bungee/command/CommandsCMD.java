package de.einfachesache.proxymanager.bungee.command;

import de.einfachesache.proxymanager.bungee.BungeeProxyManager;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandsCMD extends Command {

    public CommandsCMD() {
        super("commands", "proxymanager.*", "cmd");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender == null) return;

        RedisConnector jedis = Core.getRedisConnector();

        if (args.length == 0) {
            if (Boolean.parseBoolean(jedis.get("Commands-Disabled"))) {
                BungeeProxyManager.sendMessage(sender, "§aDu hast alle Commands aktiviert");
                jedis.set("Commands-Disabled", "false");
            } else {
                BungeeProxyManager.sendMessage(sender, "§4Du hast alle Commands deaktiviert");
                jedis.set("Commands-Disabled", "true");
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                BungeeProxyManager.sendMessage(sender, "§aDu hast alle Commands aktiviert");
                jedis.set("Commands-Disabled", "false");
            } else if (args[0].equalsIgnoreCase("off")) {
                BungeeProxyManager.sendMessage(sender, "§4Du hast alle Commands deaktiviert");
                jedis.set("Commands-Disabled", "true");
            }

        }
    }
}
