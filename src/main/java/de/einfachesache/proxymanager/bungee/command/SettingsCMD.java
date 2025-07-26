package de.einfachesache.proxymanager.bungee.command;

import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.chat.ChatElement;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SettingsCMD extends Command {

    public SettingsCMD() {
        super("settings");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender == null) return;
        if (!(sender instanceof ProxiedPlayer player)) return;

        Inventory inventory = new Inventory(InventoryType.GENERIC_9X3).title(ChatElement.ofLegacyText("§6§lSettings"));
        inventory.item(13, new ItemStack(new ItemStack(ItemType.REDSTONE).displayName(ChatElement.ofLegacyText("§4API settings"))));

        inventory.onClick(event -> player.sendMessage(new TextComponent("Aufgeführt")));

        Protocolize.playerProvider().player(player.getUniqueId()).openInventory(inventory);
        player.sendMessage(new TextComponent("§aOpening settings..."));
    }
}
