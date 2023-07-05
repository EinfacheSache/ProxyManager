package de.cubeattack.proxymanager.bungee.command;

import de.cubeattack.proxymanager.bungee.ProxyManager;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.inventory.PlayerInventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;
import dev.simplix.protocolize.data.packets.ConfirmTransaction;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SettingsCommand extends Command {

    public SettingsCommand() {
        super("settings");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender == null) return;
        if(!(sender instanceof ProxiedPlayer p))return;


        ProtocolizePlayer player = Protocolize.playerProvider().player(p.getUniqueId());

        PlayerInventory playerInventory = player.proxyInventory();
        playerInventory.update();

        Inventory inventory = new Inventory(InventoryType.GENERIC_9X3).title("§6§lSettings");
        inventory.item(13, new ItemStack(ItemType.REDSTONE_LAMP));

        inventory.onClick(event -> {

            player.sendPacket(new ConfirmTransaction((byte)event.windowId(), (short)event.actionNumber(), true));

            playerInventory.cursorItem(new ItemStack(ItemType.STONE));
            playerInventory.update();
            p.sendMessage(new TextComponent("Aufgeführt"));
        });

        Protocolize.playerProvider().player(p.getUniqueId()).openInventory(inventory);

        /*
        PlayerPosition pakete = new PlayerPosition(new Location(100, 100, 100 ,0,0 ), true);

       // p.sendPacket(pakete);
        //p.sendPacket(new HeldItemChange((short) 1)); // geht

        ScoreboardObjective scoreboardObjective = new ScoreboardObjective();
        scoreboardObjective.setName("test");
        scoreboardObjective.setAction((byte) 0);
        scoreboardObjective.setValue("{\"text\": \"GiantNetwork\"}");
        scoreboardObjective.setType(ScoreboardObjective.HealthDisplay.INTEGER);
        p.sendPacket(scoreboardObjective);


        ScoreboardDisplay scoreboardDisplay = new ScoreboardDisplay();
        scoreboardDisplay.setPosition((byte) 1);
        scoreboardDisplay.setName("test");
        p.sendPacket(scoreboardDisplay);


        ScoreboardScore scoreboardScore1 = new ScoreboardScore();
        scoreboardScore1.setItemName("Random");
        scoreboardScore1.setValue(1);
        scoreboardScore1.setScoreName("test");
        scoreboardScore1.setAction((byte) 0);
        p.sendPacket(scoreboardScore1);

        ScoreboardScore scoreboardScore2 = new ScoreboardScore();
        scoreboardScore2.setItemName("Random2");
        scoreboardScore2.setValue(2);
        scoreboardScore2.setScoreName("test");
        scoreboardScore2.setAction((byte) 0);
        p.sendPacket(scoreboardScore2);


                /*
                Title title = new net.md_5.bungee.protocol.packet.Title();
                title.setText("{" +
                "            \"text\": \"qux\"," +
                "            \"bold\": true" +
                "        }");
        title.setAction(Title.Action.TITLE);
        title.setStay(10000);
        p.sendPacket(title);

                 */

        /*

         */
        ProxyManager.getPlugin().getProxy().getPlayer(p.getUniqueId()).sendMessage(new TextComponent("Super"));
    }
}
