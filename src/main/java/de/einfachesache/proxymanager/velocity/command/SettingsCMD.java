package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.chat.ChatElement;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.kyori.adventure.text.Component;

public class SettingsCMD implements SimpleCommand {

    private final VProxyManager instance;

    public SettingsCMD(VProxyManager instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("§cThis command can only be executed as a Player"));
            return;
        }

        if (!instance.getProxy().getPluginManager().isLoaded("protocolize")) {
            source.sendMessage(Component.text("§cProtocolize need to be installed to run this command"));
            return;
        }

        Inventory inventory = new Inventory(InventoryType.GENERIC_9X3).title(ChatElement.ofLegacyText("§6§lSettings"));

        ItemStack item = new ItemStack(ItemType.REDSTONE);
        item.displayName(ChatElement.ofLegacyText("§4API settings"));
        inventory.item(13, item);


        inventory.onClick(event -> player.sendMessage(Component.text("§7Aufgeführt")));

        Protocolize.playerProvider().player(player.getUniqueId()).openInventory(inventory);
        player.sendMessage(Component.text("§aOpening settings..."));
    }


    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.*");
    }
}
