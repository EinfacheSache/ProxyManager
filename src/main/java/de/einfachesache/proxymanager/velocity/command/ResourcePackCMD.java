package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.stream.Collectors;

public class ResourcePackCMD implements SimpleCommand {

    private final Map<UUID, Collection<ResourcePackInfo>> savedPacks = new HashMap<>();

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Dieser Befehl kann nur von einem Spieler ausgeführt werden.")
                    .color(NamedTextColor.RED));
            return;
        }

        if (invocation.arguments().length != 1) {
            sendUsage(invocation);
            return;
        }

        switch (invocation.arguments()[0].toLowerCase(Locale.ROOT)) {

            case "clear": {
                Collection<ResourcePackInfo> packs = player.getAppliedResourcePacks();

                if (packs.isEmpty()) {
                    player.sendMessage(Component.text("❌ Keine Resource-Packs zum Entfernen gefunden.")
                            .color(NamedTextColor.YELLOW));
                    return;
                }

                savedPacks.put(player.getUniqueId(), packs);
                player.clearResourcePacks();
                player.sendMessage(Component.text("✔ " + packs.size() + " serverseitigen Resource-Packs entfernt.")
                        .color(NamedTextColor.RED));
                return;
            }

            case "load": {
                Collection<ResourcePackInfo> packs = savedPacks.remove(player.getUniqueId());

                if (packs == null || packs.isEmpty()) {
                    player.sendMessage(Component.text("❌ Keine Resource-Packs zum Laden gefunden.")
                            .color(NamedTextColor.YELLOW));
                    return;
                }

                packs.forEach(player::sendResourcePacks);
                player.sendMessage(Component.text("✔ " + packs.size() + " serverseitigen Resource-Packs wurden geladen.")
                        .color(NamedTextColor.GREEN));
                return;
            }

            default: {
                sendUsage(invocation);
            }
        }
    }

    private void sendUsage(Invocation invocation) {
        invocation.source().sendMessage(Component.text("Verwende /" + invocation.alias() + " (clear/load)", NamedTextColor.RED));
    }

    @Override
    public List<String> suggest(Invocation invocation) {

        List<String> options = List.of("clear", "load");
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return options;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return options.stream()
                    .filter(o -> o.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxymanager.command.resource-packs");
    }
}
