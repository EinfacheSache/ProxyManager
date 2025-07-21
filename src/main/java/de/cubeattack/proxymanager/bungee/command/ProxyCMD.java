package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.BungeeProxyManager;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.core.RedisConnector;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.chat.ChatElement;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ProxyCMD extends Command {

    public ProxyCMD() {
        super("proxy", "proxymanager.*", "pr");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {

            if ((args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl"))) {

                if (!sender.equals(ProxyServer.getInstance().getConsole())) {
                    sender.sendMessage(new TextComponent("§cThis command can only be executed via console"));
                    return;
                }

                if (Core.getDiscordAPI().getJDA() == null) {
                    Core.warn("Can't reload cause Discord JDA is null");
                    return;
                }

                Core.getDiscordAPI().loadDiscordCommands();
                Core.info("Commands successfully reloaded");
                return;
            } else if (args[0].equalsIgnoreCase("gui")) {

                if (!(sender instanceof ProxiedPlayer p)) {
                    sender.sendMessage(new TextComponent("§cThis command can only be executed as a Player"));
                    return;
                }

                RedisConnector jedis = Core.getRedisConnector();

                Inventory inventory = new Inventory(InventoryType.GENERIC_9X3).title(ChatElement.ofLegacyText("§c§lAdmin Settings"));
                ItemStack disableChatButton = new ItemStack(new ItemStack(ItemType.COMMAND_BLOCK).displayName(ChatElement.ofLegacyText("§4Chat Status")));
                ItemStack disableCommandsButton = new ItemStack(new ItemStack(ItemType.COMMAND_BLOCK).displayName(ChatElement.ofLegacyText("§4Commands Status")));

                disableChatButton.addToLore(ChatElement.ofLegacyText("§7The Chat is currently " + (Boolean.parseBoolean(jedis.get("Chat-Disabled")) ? "§cinaktive" : "§aactive")));
                disableCommandsButton.addToLore(ChatElement.ofLegacyText("§7Commands are currently " + (Boolean.parseBoolean(jedis.get("Commands-Disabled")) ? "§cinaktive" : "§aactive")));

                inventory.item(12, disableChatButton);
                inventory.item(14, disableCommandsButton);

                inventory.onClick(event -> {
                    if (12 == event.slot()) {
                        itemUpdateButton(disableChatButton, "Chat-Disabled", p);
                    } else if (14 == event.slot()) {
                        itemUpdateButton(disableCommandsButton, "Commands-Disabled", p);
                    }
                });

                Protocolize.playerProvider().player(p.getUniqueId()).openInventory(inventory);
                BungeeProxyManager.getPlugin().getProxy().getPlayer(p.getUniqueId()).sendMessage(new TextComponent("§aOpening admin settings..."));

                return;
            }
        }
        sender.sendMessage(new TextComponent("§cInvalid arguments -> /proxy [args]"));
    }

    private void itemUpdateButton(ItemStack item, String type, ProxiedPlayer p) {
        RedisConnector jedis = Core.getRedisConnector();
        jedis.set(type, String.valueOf(!Boolean.parseBoolean(jedis.get(type))));
        item.lore(0, ChatElement.ofLegacyText((type.contains("Chat") ? "§7The Chat is" : "§7Commands are") + " currently " + (Boolean.parseBoolean(jedis.get(type)) ? "§cinaktive" : "§aactive")));
        BungeeProxyManager.sendMessage(p, (type.contains("Chat") ? "§7The Chat is" : "§7Commands are") + " now " + (Boolean.parseBoolean(jedis.get(type)) ? "§cinaktive" : "§aactive"));
    }
}
