package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.inventory.PlayerInventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SettingsCMD extends Command {

    public SettingsCMD() {
        super("settings", "proxy.execute.settings");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender == null) return;
        if (!(sender instanceof ProxiedPlayer p)) return;

        ProtocolizePlayer player = Protocolize.playerProvider().player(p.getUniqueId());

        PlayerInventory playerInventory = player.proxyInventory();
        playerInventory.update();

        Inventory inventory = new Inventory(InventoryType.GENERIC_9X3).title("§6§lSettings");
        inventory.item(13, new ItemStack(ItemType.REDSTONE_LAMP).displayName("Schlüssel"));

        inventory.onClick(event -> {

            playerInventory.cursorItem(new ItemStack(ItemType.STONE));
            playerInventory.update();
            p.sendMessage(new TextComponent("Aufgeführt"));
        });

        Protocolize.playerProvider().player(p.getUniqueId()).openInventory(inventory);
        ProxyManager.getPlugin().getProxy().getPlayer(p.getUniqueId()).sendMessage(new TextComponent("Super"));
    }
}
