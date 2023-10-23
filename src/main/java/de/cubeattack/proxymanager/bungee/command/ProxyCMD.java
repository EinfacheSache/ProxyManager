package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.core.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ProxyCMD extends Command {

    public ProxyCMD() {
        super("proxy", "*", "pr");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.equals(ProxyServer.getInstance().getConsole())){
            sender.sendMessage(new TextComponent("§cThis command can only be executed via console"));
           return;
        }

        if(args.length == 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl"))){
            Core.getDiscordAPI().loadDiscordCommands();
            sender.sendMessage(new TextComponent("§aCommands successfully reloaded"));
        }else {
            sender.sendMessage(new TextComponent("§cInvalid arguments -> /proxy [args]"));
        }
    }
}
