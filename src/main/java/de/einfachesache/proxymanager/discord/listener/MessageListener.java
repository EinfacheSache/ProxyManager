package de.einfachesache.proxymanager.discord.listener;

import de.cubeattack.api.util.Logs;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    private final Map<String, Instant> lastByUser = new ConcurrentHashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (event.isFromType(ChannelType.PRIVATE)) {
            String logLine = "DM from " + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay();
            Logs.write(Path.of((Core.isMinecraftServer() ? "plugins/ProxyManager/" : "./") + "logs/DM/" + event.getAuthor().getName() + ".log"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " " + logLine);
            Core.warn(logLine);
            return;
        }

        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild()) || event.getAuthor().isBot()) return;

        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        if (!channel.getId().equals(Config.getCountingChannelID())) {
            return;
        }

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

        int correctNumber = Config.getCountingNumber() + 1;

        if (number != correctNumber) {
            lastByUser.clear();
            Config.setCountingNumber(0);
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            event.getMessage().reply(event.getAuthor().getAsMention() + " ❌ Streak failed! Wir starten wieder bei **1**. Die korrekte Zahl wäre **" + correctNumber + "** gewesen.").queue();
            return;
        }

        Config.setCountingNumber(number);
        lastByUser.put(userId, now);
        event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
    }
}