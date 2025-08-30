package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ClearResourcePackCMD implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Dieser Befehl kann nur von einem Spieler ausgeführt werden.")
                    .color(NamedTextColor.RED));
            return;
        }

        if (player.getAppliedResourcePacks().isEmpty()) {
            player.sendMessage(Component.text("ℹ️ Keine Resource-Packs zum Entfernen gefunden.")
                    .color(NamedTextColor.YELLOW));
            return;
        }

        player.clearResourcePacks();
        player.sendMessage(Component.text("✔ Alle serverseitigen Resource-Packs entfernt.")
                .color(NamedTextColor.GREEN));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxymanager.command.clear-resource-packs");
    }
}
