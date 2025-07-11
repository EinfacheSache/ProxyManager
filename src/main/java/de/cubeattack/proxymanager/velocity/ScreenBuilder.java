package de.cubeattack.proxymanager.velocity;

import net.kyori.adventure.text.Component;

public class ScreenBuilder {

    StringBuilder screen;

    public ScreenBuilder() {
        screen = new StringBuilder();
    }

    public ScreenBuilder addLine(String line) {
        screen.append(line).append("§r").append("\n");
        return this;
    }

    public Component build() {
        return Component.text(String.valueOf(screen));
    }
}