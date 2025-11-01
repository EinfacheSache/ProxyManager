package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.api.util.LogUtils;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    private final Map<String, Instant> lastByUser = new ConcurrentHashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (event.isFromType(ChannelType.PRIVATE)) {
            handleDM(event);
            return;
        }

        String guildID = event.getGuild().getId();
        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();

        if (!Config.getGuildIDs().contains(guildID)) {
            return;
        }

        if (channel.getId().equals(Config.getCountingChannelID(guildID))) {
            handleCounting(event);
            return;
        }

        if (channel.getId().equals(Config.getDiscordServerProfile(guildID).getWhitelistChannelId())) {
            handleMessageInWhitelistChannel(event);
        }
    }


    private void handleDM(MessageReceivedEvent event) {
        String logLine = "DM from " + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay();
        LogUtils.write(Path.of((Core.isMinecraftServer() ? "plugins/ProxyManager/" : "./") + "logs/DM/" + event.getAuthor().getName() + ".log"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " " + logLine);
        Core.warn(logLine);
    }

    private void handleMessageInWhitelistChannel(MessageReceivedEvent event) {
        if (event.getMember() == null) return;
        if (event.getMember().getPermissions().contains(Permission.MODERATE_MEMBERS)) return;

        event.getMessage().delete().queue();
        event.getMessage()
                .reply("Bitte Verwende `/whitelist <Ingame-Name>`")
                .queue(sentMessage -> sentMessage.delete().queueAfter(12, TimeUnit.SECONDS));
    }

    private void handleCounting(MessageReceivedEvent event) {

        String guildID = event.getGuild().getId();
        String content = event.getMessage().getContentRaw().trim().toLowerCase().replaceAll("pi", "3");

        if (!content.matches("\\d+")) {
            event.getMessage().delete().queue();
            return;
        }

        String userId = event.getAuthor().getId();
        Instant now = Instant.now();
        int number = Integer.parseInt(content);

        Instant last = lastByUser.get(userId);
        if (last != null && Duration.between(last, now).toHours() < 1) {
            event.getMessage().reply("<@" + userId + ">, du darfst nur jede Stunde eine Zahl posten.").queue(
                    sentMessage -> sentMessage.delete().queueAfter(10, TimeUnit.SECONDS));
            event.getMessage().delete().queue();
            return;
        }

        int correctNumber = Config.getCountingNumber(guildID) + 1;

        if (number != correctNumber) {
            lastByUser.clear();
            Config.setCountingNumber(guildID, 0);
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            event.getMessage().reply(event.getAuthor().getAsMention() + " ❌ Streak failed! Wir starten wieder bei **1**. Die korrekte Zahl wäre **" + correctNumber + "** gewesen.").queue();

            String logLine = event.getGuild().getName() + " | " + event.getAuthor().getName() + " failed at " + number + " (should be " + correctNumber + ").";
            LogUtils.write(Path.of((Core.isMinecraftServer() ? "plugins/ProxyManager/" : "./") + "logs/counting.log"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " " + logLine);
            Core.warn(logLine);

            return;
        }

        Config.setCountingNumber(guildID, number);
        lastByUser.put(userId, now);
        event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();

        String logLine = event.getGuild().getName() + " | " + event.getAuthor().getName() + " continued the count with " + number + ".";
        LogUtils.write(Path.of((Core.isMinecraftServer() ? "plugins/ProxyManager/" : "./") + "logs/counting.log"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " " + logLine);
    }
}