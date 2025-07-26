package de.einfachesache.proxymanager.bungee;


import net.md_5.bungee.api.chat.TextComponent;

public class ScreenBuilder {

    StringBuilder screen;

    public ScreenBuilder() {
        screen = new StringBuilder();
    }

    public ScreenBuilder addLine(String line) {
        screen.append(line).append("§r").append("\n");
        return this;
    }

    public TextComponent build() {
        return new TextComponent(String.valueOf(screen));
    }
}
