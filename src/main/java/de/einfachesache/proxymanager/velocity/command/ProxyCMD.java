package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import de.einfachesache.proxymanager.velocity.VelocityProxyManager;
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

    private static final List<String> SUB_COMMANDS = Arrays.asList("reload", "send");

    private final VelocityProxyManager velocityProxyManager;

    public ProxyCMD(VelocityProxyManager velocityProxyManager) {
        this.velocityProxyManager = velocityProxyManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();


        if (invocation.alias().equalsIgnoreCase("proxygui")) {
            openAdminGUI(source);
            return;
        }

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                if (!(source instanceof ConsoleCommandSource)) {
                    source.sendMessage(Component.text("§cThis command can only be executed via console"));
                    return;
                }

                if (Core.getDiscordAPI().getJDA() == null) {
                    Core.warn("Can't reload cause Discord JDA is null");
                    return;
                }

                Core.getDiscordAPI().loadDiscordCommands();
                Core.info("Commands successfully reloaded");
                return;

            }
        }

        if(args.length == 3) {

            String targetName = invocation.arguments()[1];
            String serverName = invocation.arguments()[2];

            Optional<Player> target = velocityProxyManager.getProxy().getPlayer(targetName);
            Optional<RegisteredServer> server = velocityProxyManager.getProxy().getServer(serverName);


            if (target.isEmpty()) {
                invocation.source().sendMessage(Component.text("Spieler '" + targetName + "' wurde nicht gefunden.",  NamedTextColor.RED));
                return;
            }

            if(server.isEmpty()){
                invocation.source().sendMessage(Component.text("Server '" + serverName + "' wurde nicht gefunden.",  NamedTextColor.RED));
                return;
            }

            if(target.get().getCurrentServer().isPresent() && target.get().getCurrentServer().get().getServer().equals(server.get())) {
                invocation.source().sendMessage(Component.text(targetName + " ist bereits auf dem " + serverName + ".", NamedTextColor.YELLOW));
                return;
            }

            target.get().createConnectionRequest(server.get()).connect();
            invocation.source().sendMessage(Component.text(targetName + " wurde zu " + serverName + " gesendet.", NamedTextColor.GREEN));
            return;
        }

        source.sendMessage(Component.text("§cInvalid arguments -> /proxy [args]"));
    }

    private void openAdminGUI(CommandSource source) {

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("§cThis command can only be executed as a Player"));
            return;
        }

        if (!velocityProxyManager.getProxy().getPluginManager().isLoaded("protocolize")) {
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
        } else if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return SUB_COMMANDS.stream()
                    .filter(sub -> sub.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        else if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            return velocityProxyManager.getProxy().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        else if (args.length == 3) {
            String prefix = args[2].toLowerCase();
            return velocityProxyManager.getProxy().getAllServers().stream()
                    .map(RegisteredServer::getServerInfo)
                    .map(ServerInfo::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.*") || (invocation.source() instanceof Player player && player.getUniqueId().equals(Core.DEV_UUID));
    }
}