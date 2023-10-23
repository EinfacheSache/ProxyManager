package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import de.cubeattack.proxymanager.core.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class GlobalMuteCMD extends Command {

    public GlobalMuteCMD() {
        super("gmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender == null) return;

        if(!sender.hasPermission("proxymanager.chat")) {
            ProxyManager.sendMessage(sender,"§4Dazu hast du keine Rechte");
            return;
        }

        if(!Core.getRedisConnector().getJedis().isConnected() || Core.getRedisConnector().getJedis().isBroken()){
           ProxyManager.sendMessage(sender, "§cRedis-Server connection failed");
           return;
        }

        if (args.length == 0) {
            if (Boolean.parseBoolean(Core.getRedisConnector().getJedis().get("Chat-Disabled"))) {
                ProxyManager.sendMessage(sender,"§aDu hast den Chat aktiviert");
                Core.getRedisConnector().getJedis().set("Chat-Disabled", "false");
            } else {
                ProxyManager.sendMessage(sender,"§4Du hast den Chat deaktiviert");
                Core.getRedisConnector().getJedis().set("Chat-Disabled", "true");
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("off")) {
                ProxyManager.sendMessage(sender,"§aDu hast den Chat aktiviert");
                Core.getRedisConnector().getJedis().set("Chat-Disabled", "false");
            } else if (args[0].equalsIgnoreCase("on")) {
                ProxyManager.sendMessage(sender,"§4Du hast den Chat deaktiviert");
                Core.getRedisConnector().getJedis().set("Chat-Disabled", "true");
            }

        }
    }
}
