package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.BungeeProxyManager;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.core.RedisConnector;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandsCMD extends Command {

    public CommandsCMD() {
        super("commands", "proxy.execute.commands");
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
