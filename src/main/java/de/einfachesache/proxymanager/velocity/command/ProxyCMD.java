package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.chat.ChatElement;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.stream.Collectors;

public class ProxyCMD implements SimpleCommand {

    private final List<String> SUB_COMMANDS = Arrays.asList("reload", "send");
    private static final List<String> RELOAD_SUBS = List.of("config", "discord");

    private final VProxyManager instance;

    public ProxyCMD(VProxyManager instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();


        if (invocation.alias().equalsIgnoreCase("proxygui")) {
            openAdminGUI(source);
            return;
        }

        if (args.length >= 1) {

            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {

                if (args.length == 1) {
                    source.sendMessage(Component.text("Reload: Config + Discord …").color(NamedTextColor.GRAY));
                    Config.reloadFilesAsync();
                    Core.getDiscordAPI().reloadGuildsAsync();
                    source.sendMessage(Component.text("Reload ausgeführt (Config + Discord).").color(NamedTextColor.GREEN));
                    return;
                }

                String sub = args[1].toLowerCase(Locale.ROOT);
                switch (sub) {
                    case "config" -> {
                        source.sendMessage(Component.text("Reload: Config …").color(NamedTextColor.GRAY));
                        Config.reloadFilesAsync();
                        source.sendMessage(Component.text("Config neu geladen.").color(NamedTextColor.GREEN));
                        return;
                    }
                    case "discord", "jda" -> {
                        source.sendMessage(Component.text("Reload: Discord …").color(NamedTextColor.GRAY));
                        Core.getDiscordAPI().reloadGuildsAsync();
                        source.sendMessage(Component.text("Discord neu geladen.").color(NamedTextColor.GREEN));
                        return;
                    }
                    default -> {
                        source.sendMessage(Component.text("Unbekanntes Argument: " + sub).color(NamedTextColor.RED));
                        source.sendMessage(Component.text("Nutzung: /proxy reload [config|discord]").color(NamedTextColor.RED));
                        return;
                    }
                }
            }
        }

        source.sendMessage(Component.text("§cInvalid arguments -> /proxy [args]"));
    }

    private void openAdminGUI(CommandSource source) {

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("§cThis command can only be executed as a Player"));
            return;
        }

        if (!instance.getProxy().getPluginManager().isLoaded("protocolize")) {
            source.sendMessage(Component.text("§cProtocolize need to be installed to run this command"));
            return;
        }

        RedisConnector jedis = Core.getRedisConnector();
        UUID uuid = player.getUniqueId();

        Inventory inventory = new Inventory(InventoryType.GENERIC_9X3)
                .title(ChatElement.ofLegacyText("§c§lAdmin Settings"));

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

        item.lore(0, ChatElement.ofLegacyText((type.contains("Chat") ? "§7The Chat is" : "§7Commands are") + " currently " + (newState ? "§cinaktive" : "§aactive")));
        player.sendMessage(Component.text((type.contains("Chat") ? "§7The Chat is" : "§7Commands are") + " now " + (newState ? "§cinaktive" : "§aactive")));
    }


    @Override
    public List<String> suggest(final Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return SUB_COMMANDS;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return SUB_COMMANDS.stream()
                    .filter(sub -> sub.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        String first = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);

            if (first.equals("send")) {
                return instance.getProxy().getAllPlayers().stream()
                        .map(Player::getUsername)
                        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                        .collect(Collectors.toList());
            }

            if (first.equals("reload") || first.equals("rl")) {
                return RELOAD_SUBS.stream()
                        .filter(sub -> sub.startsWith(prefix))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        }

        if (args.length == 3) {
            if (first.equals("send")) {
                String prefix = args[2].toLowerCase(Locale.ROOT);
                return instance.getProxy().getAllServers().stream()
                        .map(RegisteredServer::getServerInfo)
                        .map(ServerInfo::getName)
                        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("*");
    }
}