package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import de.cubeattack.proxymanager.core.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandsCMD extends Command {

    public CommandsCMD() {
        super("commands");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender == null) return;

        if(!sender.hasPermission("proxymanager.commands")) {
            ProxyManager.sendMessage(sender,"§4Dazu hast du keine Rechte");
            return;
        }

        if(!Core.getRedisConnector().getJedis().isConnected() || Core.getRedisConnector().getJedis().isBroken()){
            ProxyManager.sendMessage(sender,"§cRedis-Server connection failed");
            return;
        }

        if (args.length == 0) {
            if (Boolean.parseBoolean(Core.getRedisConnector().getJedis().get("Commands-Disabled"))) {
                ProxyManager.sendMessage(sender,"§aDu hast alle Commands aktiviert");
                Core.getRedisConnector().getJedis().set("Commands-Disabled", "false");
            } else {
                ProxyManager.sendMessage(sender,"§4Du hast alle Commands deaktiviert");
                Core.getRedisConnector().getJedis().set("Commands-Disabled", "true");
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                ProxyManager.sendMessage(sender,"§aDu hast alle Commands aktiviert");
                Core.getRedisConnector().getJedis().set("Commands-Disabled", "false");
            } else if (args[0].equalsIgnoreCase("off")) {
                ProxyManager.sendMessage(sender,"§4Du hast alle Commands deaktiviert");
                Core.getRedisConnector().getJedis().set("Commands-Disabled", "true");
            }
        }
    }
}
