package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import de.cubeattack.proxymanager.core.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class CommandsCMD extends Command {

    public CommandsCMD() {
        super("commands", "proxy.execute.commands");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender == null) return;
        try(Jedis jedis = Core.getRedisConnector().getJedisPool().getResource()) {

            if (!jedis.isConnected() || jedis.isBroken()) {
                ProxyManager.sendMessage(sender, "§cRedis-Server connection failed");
                return;
            }

            if (args.length == 0) {
                if (Boolean.parseBoolean(jedis.get("Commands-Disabled"))) {
                    ProxyManager.sendMessage(sender, "§aDu hast alle Commands aktiviert");
                    jedis.set("Commands-Disabled", "false");
                } else {
                    ProxyManager.sendMessage(sender, "§4Du hast alle Commands deaktiviert");
                    jedis.set("Commands-Disabled", "true");
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("on")) {
                    ProxyManager.sendMessage(sender, "§aDu hast alle Commands aktiviert");
                    jedis.set("Commands-Disabled", "false");
                } else if (args[0].equalsIgnoreCase("off")) {
                    ProxyManager.sendMessage(sender, "§4Du hast alle Commands deaktiviert");
                    jedis.set("Commands-Disabled", "true");
                }
            }
        }
    }
}
