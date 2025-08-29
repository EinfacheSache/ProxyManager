package de.einfachesache.proxymanager.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;

public class MessageUtils {

    private static final MessageEmbed TEMPLATE = new EmbedBuilder()
            .setColor(Color.CYAN)
            .setFooter("Made by EinfacheSache | Tom")
            .build();

    public static EmbedBuilder getDefaultEmbed() {
        return new EmbedBuilder(TEMPLATE).setTimestamp(Instant.now());
    }

    public static EmbedBuilder getErrorEmbed() {
        return new EmbedBuilder(TEMPLATE).setTimestamp(Instant.now()).setColor(Color.RED);
    }
}
