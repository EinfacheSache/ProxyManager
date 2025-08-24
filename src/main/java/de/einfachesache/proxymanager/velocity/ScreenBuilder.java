package de.einfachesache.proxymanager.velocity;

import de.einfachesache.proxymanager.core.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ScreenBuilder {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private static final Component maintenanceScreen = new ScreenBuilder()
            .addLine("§c§lWartungsarbeiten")
            .addEmptyLine()
            .addLine("§7Unser Server befindet sich derzeit im §eWartungsmodus§7.")
            .addLine("§7Bitte schaue später noch einmal vorbei.")
            .addEmptyLine()
            .addLine("§7Weitere Infos findest du auf unserem Discord:")
            .addLine("§b" + Config.getServerDomainName() + "/discord")
            .build();


    StringBuilder screen = new StringBuilder();

    public ScreenBuilder addLine(String line) {
        screen.append(line).append("\n");
        return this;
    }

    public ScreenBuilder addEmptyLine() {
        screen.append("\n");
        return this;
    }

    public Component build() {
        return LEGACY.deserialize(screen.toString().strip());
    }

    public static Component getMaintenanceScreen() {
        return maintenanceScreen;
    }
}