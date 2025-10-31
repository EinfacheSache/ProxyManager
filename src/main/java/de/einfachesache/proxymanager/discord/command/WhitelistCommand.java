package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.api.minecraft.MinecraftAPI;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.MessageUtils;
import de.einfachesache.proxymanager.velocity.listener.LoginAccessControlListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WhitelistCommand extends ListenerAdapter {

    private final DiscordAPI discordAPI;

    private final Pattern MC_NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    public WhitelistCommand(DiscordAPI discordAPI) {
        this.discordAPI = discordAPI;
    }

    public record WhitelistResult(String message, Color color, boolean ephemeral) {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;

        switch (event.getName()) {
            case "whitelist-sync" -> handleSync(event);
            case "whitelist-list" -> handleList(event);
            case "whitelist" -> handleWhitelist(event);
        }
    }

    private void handleSync(SlashCommandInteractionEvent event) {

        event.deferReply(true).queue();

        var shouldBeWhitelisted = Config.getWhitelistedPlayers().keySet();
        String auditReason = "Whitelist sync – align role with config by " + event.getUser().getAsTag();

        discordAPI.getGuilds().values().forEach(guild -> {

            Role whitelistRole = guild.getRoleById(Config.getWhitelistedRoleID(guild.getId()));
            if (whitelistRole == null) return;

            Set<String> currentRoleHolders = getDiscordIdsWithRoleCached(guild, whitelistRole);

            Set<String> toAdd = new HashSet<>(shouldBeWhitelisted);
            toAdd.removeAll(currentRoleHolders);

            Set<String> toRemove = new HashSet<>(currentRoleHolders);
            toRemove.removeAll(shouldBeWhitelisted);

            for (String discordId : toAdd) {
                guild.retrieveMemberById(discordId).queue(
                        member -> guild.addRoleToMember(member, whitelistRole)
                                .reason(auditReason)
                                .queue(_ok -> {
                                }, _err -> {
                                }),
                        _err -> {
                        }
                );
            }

            for (String discordId : toRemove) {
                guild.retrieveMemberById(discordId).queue(
                        member -> guild.removeRoleFromMember(member, whitelistRole)
                                .reason(auditReason)
                                .queue(_ok -> {
                                }, _err -> {
                                }),
                        _err -> {
                        }
                );
            }
        });

        event.getHook().setEphemeral(true).sendMessageEmbeds(
                        MessageUtils
                                .getDefaultEmbed()
                                .setTitle("Whitelist-Sync")
                                .setDescription("Whitelist-Sync läuft. Rollen werden nun aktualisiert.")
                                .build())
                .queue();
    }


    private void handleList(SlashCommandInteractionEvent event) {

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

            String discordId = entry.getKey();
            String minecraftName = entry.getValue();

            if (!discordId.matches("^[0-9]+$")) {
                continue;
            }

            if (fieldsInPage == 25) {
                pages.add(page.build());
                page = MessageUtils.getDefaultEmbed()
                        .setTitle("Whitelist – Übersicht");
                fieldsInPage = 0;
            }

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

        event.deferReply(true).queue(hook -> {
            int i = 0;
            while (i < pages.size()) {
                List<MessageEmbed> batch = new ArrayList<>();
                int batchLen = 0;

                while (i < pages.size() && batch.size() < 10) {
                    MessageEmbed em = pages.get(i);
                    int emLen = em.getLength();

                    if (!batch.isEmpty() && batchLen + emLen > 5900) break;

                    batch.add(em);
                    batchLen += emLen;
                    i++;
                }
                hook.sendMessageEmbeds(batch).setEphemeral(true).queue();
            }

            hook.setEphemeral(true).sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                            .setTitle("Whitelist – Übersicht")
                            .setDescription("Aktuell sind **" + map.size() + " Spieler** whitelisted")
                            .build())
                    .queue();
        });
    }

    private void handleWhitelist(SlashCommandInteractionEvent event) {

        if (event.getMember() == null) return;

        EmbedBuilder embed = MessageUtils.getDefaultEmbed().setTitle("Event Whitelist");
        String whitelistChannelId = Config.getDiscordServerProfile(Objects.requireNonNull(event.getGuild()).getId()).getWhitelistChannelId();

        if (whitelistChannelId == null || whitelistChannelId.equalsIgnoreCase("-1")) {
            event.replyEmbeds(embed.setDescription("❌ Whitelisten ist deaktiviert — momentan kannst du dich nicht whitelisten.").setColor(Color.RED).build()).setEphemeral(true).queue();
            return;
        }

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

            boolean alreadyWhitelisted = Config.getWhitelistedPlayers().values().stream().anyMatch(mcName -> mcName.equalsIgnoreCase(name));
            if (alreadyWhitelisted) {
                return new WhitelistResult("ℹ️ Der Spieler `" + name + "` ist bereits whitelisted.", Color.YELLOW, true);
            }

            String oldName = Config.getWhitelistedPlayers().getOrDefault(discordId, null);

            Config.removeFromWhitelistByUser(discordId);
            Config.whitelistPlayer(event.getGuild().getId(), discordId, name);

            Core.getDiscordAPI().getGuilds().forEach((guildID, guild) -> {

                Role whitelistedRole = guild.getRoleById(Config.getWhitelistedRoleID(guildID));
                if (whitelistedRole == null) return;

                guild.addRoleToMember(event.getUser(), whitelistedRole)
                        .reason("Auto-Role on Whitelist")
                        .queue(success -> { }, error -> { });
            });

            if (oldName == null) {
                return new WhitelistResult("✅ `" + name + "` wurde whitelisted.", Color.GREEN, false);
            } else {
                if (Core.isMinecraftServer()) {
                    LoginAccessControlListener.sendLimboOnWhitelistRemove(oldName);
                }
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
            Core.severe(ex.getMessage(), ex);
            return null;
        });
    }

    public static Set<String> getDiscordIdsWithRoleCached(Guild guild, Role whitelistRole) {
        List<Member> roleHolders = guild.getMembersWithRoles(whitelistRole);
        return roleHolders.stream()
                .map(Member::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
