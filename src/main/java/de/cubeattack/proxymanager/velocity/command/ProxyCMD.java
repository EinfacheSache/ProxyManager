package de.cubeattack.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.core.RedisConnector;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.chat.ChatElement;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ProxyCMD implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                if (!(source instanceof ConsoleCommandSource)) {
                    source.sendMessage(Component.text("§cThis command can only be executed via console"));
                    return;
                }

                if(Core.getDiscordAPI().getJDA() == null){
                    source.sendMessage(Component.text("Can't reload cause Discord JDA is null"));
                    return;
                }

                Core.getDiscordAPI().loadDiscordCommands();
                source.sendMessage(Component.text("§aCommands successfully reloaded"));
                return;

            } else if (args[0].equalsIgnoreCase("gui")) {
                if (!(source instanceof Player player)) {
                    source.sendMessage(Component.text("§cThis command can only be executed as a Player"));
                    return;
                }

                openAdminGUI(player);
                return;
            }
        }

        source.sendMessage(Component.text("§cInvalid arguments -> /proxy [args]"));
    }

    private void openAdminGUI(Player player) {
        RedisConnector jedis = Core.getRedisConnector();
        UUID uuid = player.getUniqueId();

        Inventory inventory = new Inventory(InventoryType.GENERIC_9X3)
                .title(ChatElement.ofLegacyText("§c§lAdminSettings"));

        // Items erstellen
        ItemStack disableChatButton = new ItemStack(ItemType.COMMAND_BLOCK);
        disableChatButton.displayName(ChatElement.ofLegacyText("§4Chat Status"));
        disableChatButton.addToLore(ChatElement.ofLegacyText("§7The Chat is currently " +
                (Boolean.parseBoolean(jedis.get("Chat-Disabled")) ? "§cinaktive" : "§aactive")));

        ItemStack disableCommandsButton = new ItemStack(ItemType.COMMAND_BLOCK);
        disableCommandsButton.displayName(ChatElement.ofLegacyText("§4Commands Status"));
        disableCommandsButton.addToLore(ChatElement.ofLegacyText("§7Commands are currently " +
                (Boolean.parseBoolean(jedis.get("Commands-Disabled")) ? "§cinaktive" : "§aactive")));

        inventory.item(12, disableChatButton);
        inventory.item(14, disableCommandsButton);

        inventory.onClick(event -> {
            if (event.slot() == 12) {
                itemUpdateButton(disableChatButton, "Chat-Disabled", player);
            } else if (event.slot() == 14) {
                itemUpdateButton(disableCommandsButton, "Commands-Disabled", player);
            }
        });

        Protocolize.playerProvider().player(uuid).openInventory(inventory);
        player.sendMessage(Component.text("§aOpening admin settings..."));
    }

    private void itemUpdateButton(ItemStack item, String type, Player player) {
        RedisConnector jedis = Core.getRedisConnector();
        boolean newState = !Boolean.parseBoolean(jedis.get(type));
        jedis.set(type, String.valueOf(newState));

        String statusText = (type.contains("Chat") ? "§7The Chat is" : "§7Commands are") +
                " currently " + (newState ? "§cinaktive" : "§aactive");

        item.lore(0, ChatElement.ofLegacyText(statusText));
        player.sendMessage(Component.text(statusText));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.*") || (invocation.source() instanceof Player player && player.getUniqueId().equals(Core.ALLOWED_UUID));
    }
}