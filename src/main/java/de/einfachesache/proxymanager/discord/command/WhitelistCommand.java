package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.api.minecraft.MinecraftAPI;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class WhitelistCommand extends ListenerAdapter {

    private final Pattern MC_NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    public record WhitelistResult(String message, Color color, boolean ephemeral) {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;

        switch (event.getName()) {
            case "whitelist-list" -> handleList(event);
            case "whitelist" -> handleWhitelist(event);
        }
    }

    private void handleList(SlashCommandInteractionEvent event) {

        if (!event.getName().equalsIgnoreCase("whitelist-list")) return;

        var guild = event.getGuild();
        var map = Config.getWhitelistedPlayers();

        if (map == null || map.isEmpty()) {
            event.replyEmbeds(
                    MessageUtils.getErrorEmbed()
                            .setTitle("Whitelist – Übersicht")
                            .setDescription("*Keine Einträge*")
                            .build()
            ).setEphemeral(true).queue();
            return;
        }

        List<MessageEmbed> pages = new ArrayList<>();
        EmbedBuilder page = MessageUtils.getDefaultEmbed()
                .setTitle("Whitelist – Übersicht");

        int fieldsInPage = 0;
        for (var entry : map.entrySet().stream()
                .sorted(
                        Map.Entry.<String, String>comparingByValue(String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(Map.Entry.comparingByKey())
                ).toList()) {

            if (fieldsInPage == 25) {
                pages.add(page.build());
                page = MessageUtils.getDefaultEmbed()
                        .setTitle("Whitelist – Übersicht");
                fieldsInPage = 0;
            }

            String discordId = entry.getKey();
            String minecraftName = entry.getValue();

            var member = guild != null ? guild.getMemberById(discordId) : null;
            var user = event.getJDA().getUserById(discordId);
            String fieldName = "`" + minecraftName + "`";
            String fieldValue = (member != null)
                    ? member.getAsMention()
                    : ((user != null
                    ? user.getName() : "") + " `<@" + discordId + ">`");

            page.addField(fieldName, fieldValue, false);
            fieldsInPage++;
        }

        if (fieldsInPage > 0) {
            pages.add(page.build());
        }

        event.replyEmbeds(pages).setEphemeral(true).queue();
    }

    private void handleWhitelist(SlashCommandInteractionEvent event) {

        if (!event.getName().equalsIgnoreCase("whitelist")) return;

        EmbedBuilder embed = MessageUtils.getDefaultEmbed().setTitle("Event Whitelist");
        String whitelistChannelId = Config.getDiscordServerProfile(Objects.requireNonNull(event.getGuild()).getId()).getWhitelistChannelId();

        if (!event.getChannel().getId().equals(whitelistChannelId)) {
            event.replyEmbeds(embed.setDescription("❌ Falscher Kanal. Bitte nutze <#" + whitelistChannelId + "> für `/whitelist <name>`.").setColor(Color.RED).build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping nameOpt = event.getOption("name");
        if (nameOpt == null) {
            event.replyEmbeds(embed.setDescription("❌ Bitte gib einen Namen an: `/whitelist <name>`.").setColor(Color.RED).build()).setEphemeral(true).queue();
            return;
        }

        String name = nameOpt.getAsString().trim();
        if (!MC_NAME.matcher(name).matches()) {
            event.replyEmbeds(embed.setDescription("❌ Ungültiger Minecraft-Name. Erlaubt: 3–16 Zeichen (A–Z, 0–9, _).").setColor(Color.RED).build()).setEphemeral(true).queue();
            return;
        }

        String discordId = event.getUser().getId();
        CompletableFuture.supplyAsync(() -> {

            if (MinecraftAPI.loadUUID(name) == null) {
                return new WhitelistResult("ℹ️ Der Spieler `" + name + "` existiert nicht.", Color.RED, true);
            }

            boolean alreadyWhitelisted = Config.getWhitelistedPlayers().values().stream().anyMatch(mapName -> mapName.equalsIgnoreCase(name));
            if (alreadyWhitelisted) {
                return new WhitelistResult("ℹ️ Der Spieler `" + name + "` ist bereits whitelisted.", Color.YELLOW, true);
            }

            String oldName = Config.getWhitelistedPlayers().getOrDefault(discordId, null);
            Config.whitelistPlayer(discordId, name);

            if (oldName == null) {
                return new WhitelistResult("✅ `" + name + "` wurde whitelisted.", Color.GREEN, false);
            } else {
                return new WhitelistResult("🔄 Aktualisiert: `" + oldName + "` ➜ `" + name + "`. Neuer Spielername whitelisted.", Color.CYAN, false);
            }
        }).thenAccept(result ->
                event.replyEmbeds(embed
                                .setDescription(result.message)
                                .setColor(result.color)
                                .build())
                        .setEphemeral(result.ephemeral)
                        .queue()
        ).exceptionally(ex -> {
            event.replyEmbeds(embed.setDescription("❌ Unerwarteter Fehler: " + ex.getMessage()).setColor(Color.RED).build()).queue();
            return null;
        });
    }
}
