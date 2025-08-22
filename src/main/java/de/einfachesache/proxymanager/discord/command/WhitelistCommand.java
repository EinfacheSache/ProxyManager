package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.api.minecraft.MinecraftAPI;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class WhitelistCommand extends ListenerAdapter {

    private final Pattern MC_NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    public record WhitelistResult(String message, Color color, boolean ephemeral) {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getName().equalsIgnoreCase("whitelist")) return;

        EmbedBuilder embed = MessageUtils.getDefaultEmbed().setTitle("Event Whitelist");
        String whitelistChannelId = Config.getDiscordServerProfile(event.getGuild().getId()).getWhitelistChannelId();

        if (!event.getChannel().getId().equals(whitelistChannelId)) {
            event.replyEmbeds( embed.setDescription("❌ Falscher Kanal. Bitte nutze <#" + whitelistChannelId + "> für `/whitelist <name>`.").setColor(Color.RED).build()).setEphemeral(true).queue();
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
