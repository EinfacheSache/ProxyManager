package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.api.minecraft.MinecraftAPI;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.MessageUtils;
import de.einfachesache.proxymanager.discord.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class LookupCommand extends ListenerAdapter {

    private static final String NAMEMC_URl = "https://de.namemc.com/profile/";
    private static final String MINOTAR_URL = "https://minotar.net/helm/";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getName().equalsIgnoreCase("lookup")) return;

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();
        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
        User user = new User(MinecraftAPI.loadUUID(name), name);

        embedBuilder.setDescription("Loading Data from " + user.getName());

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).flatMap(it -> {

                    if (user.getUUID() == null) {
                        return event.getHook().editOriginalEmbeds(MessageUtils.getErrorEmbed()
                                .setTitle("🔍 Nutzer nicht gefunden")
                                .setDescription("Der Nutzername **" + name + "** konnte nicht gefunden werden.")
                                .addField("Mögliche Ursachen:",
                                        """
                                                • Tippfehler im Namen
                                                • Der Nutzer existiert nicht oder ist nicht registriert
                                                """, false).build());
                    }

                    if (Core.getDatasource().isClosed()) {
                        return event.getHook().editOriginalEmbeds(MessageUtils.getErrorEmbed()
                                .setTitle("❌ Verbindungsfehler")
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
        embedBuilder.setTitle("Infos zu " + user.getName(), NAMEMC_URl + user.getName());
        embedBuilder.setThumbnail(MINOTAR_URL + user.getName());
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
