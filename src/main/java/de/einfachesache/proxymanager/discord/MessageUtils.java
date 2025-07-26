package de.einfachesache.proxymanager.discord;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.ZonedDateTime;

public class MessageUtils {

    public static EmbedBuilder getDefaultEmbed() {
        return new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTimestamp(ZonedDateTime.now())
                .setFooter("Made by EinfacheSache | Tom");
    }
}
