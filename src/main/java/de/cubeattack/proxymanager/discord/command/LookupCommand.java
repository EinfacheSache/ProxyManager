package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.api.minecraft.MinecraftAPI;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import de.cubeattack.proxymanager.discord.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class LookupCommand extends ListenerAdapter {

    private static final String namemc = "https://de.namemc.com/profile/";
    private static final String minotar = "https://minotar.net/helm/";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("lookup")) return;

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();
        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
        User user = new User(MinecraftAPI.loadUUID(name), name);

        if (user.getUUID() == null) {
            embedBuilder
                    .setTitle("🔍 Nutzer nicht gefunden")
                    .setColor(Color.RED)
                    .setDescription("Der Nutzername **" + name + "** konnte nicht gefunden werden.")
                    .addField("Mögliche Ursachen:",
                             """
                                    • Tippfehler im Namen
                                    • Der Nutzer existiert nicht oder ist nicht registriert
                                    """,
                            false);
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        embedBuilder.setDescription("Loading Data from " + user.getName());
        embedBuilder.setColor(Color.YELLOW);

        event.replyEmbeds(embedBuilder.build()).flatMap(it -> {
                    if (Core.getDatasource().isClosed()) {
                        embedBuilder.setColor(Color.RED);
                        return event.getHook().editOriginalEmbeds(embedBuilder
                                .setTitle("❌ Verbindungsfehler")
                                .setColor(Color.RED)
                                .setDescription("Die Verbindung zur MySQL-Datenbank konnte nicht hergestellt werden.")
                                .build());
                    }
                    return event.getHook().editOriginalEmbeds(getEmbedBuilder(user, embedBuilder).build());
                })
                .queue();
    }

    private EmbedBuilder getEmbedBuilder(User user, EmbedBuilder embedBuilder) {

        user.loadDataFromMySQL();

        embedBuilder.setDescription("");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setTitle("Infos zu " + user.getName(), namemc + user.getName());
        embedBuilder.setThumbnail(minotar + user.getName());
        embedBuilder.addField("UUID", user.getUUID().toString(), false);
        embedBuilder.addField("Rang", user.getRang(), false);
        embedBuilder.addField("Weight", user.getWeight(), false);
        embedBuilder.addField("Coins", user.getCoins(), false);
        embedBuilder.addField("Playtime", user.getPlaytime(), false);
        embedBuilder.addField("Webaccount", user.hasWebAccount(), false);
        embedBuilder.addField("Mute", user.isMuted(), false);
        embedBuilder.addField("Ban", user.isBanned(), false);
        return embedBuilder;
    }
}
