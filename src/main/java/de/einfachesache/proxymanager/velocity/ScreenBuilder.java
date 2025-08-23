package de.einfachesache.proxymanager.velocity;

import de.einfachesache.proxymanager.core.Config;
import net.kyori.adventure.text.Component;

public class ScreenBuilder {

    StringBuilder screen;
    Component maintenanceScreen = new ScreenBuilder()
            .addLine("§c§lWartungsarbeiten")
            .addLine("")
            .addLine("§7Unser Server befindet sich derzeit im §eWartungsmodus§7.")
            .addLine("§7Bitte schaue später noch einmal vorbei.")
            .addLine("")
            .addLine("§7Weitere Infos findest du auf unserem Discord:")
            .addLine("§b" + Config.getServerDomainName() + "/discord")
            .build();

    public ScreenBuilder() {
        screen = new StringBuilder();
    }

    public ScreenBuilder addLine(String line) {
        screen.append(line).append("§r").append("\n");
        return this;
    }

    public Component getMaintenanceScreen() {
         return maintenanceScreen;
    }

    public Component build() {
        return Component.text(String.valueOf(screen));
    }
}