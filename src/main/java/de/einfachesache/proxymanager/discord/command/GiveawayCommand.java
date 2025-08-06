package de.einfachesache.proxymanager.discord.command;

import de.cubeattack.api.API;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GiveawayCommand extends ListenerAdapter {

    private final DiscordAPI discordAPI;

    private final ChronoUnit COMMAND_TIMEUNIT = ChronoUnit.HOURS;
    private ScheduledFuture<?> rollGiveawayFuture;
    private ScheduledFuture<?> delayedGiveawayFuture;

    public GiveawayCommand(DiscordAPI discordAPI) throws InterruptedException {
        this.discordAPI = discordAPI;

        Config.getGuildIDs().forEach(guildID -> {
            Guild guild = discordAPI.getJDA().getGuildById(guildID);

            if (guild == null) {
                return;
            }

            long endMillis = Config.getGiveawayEndtimeInMilli(guildID);
            if (endMillis == -1) return;

            long timeLeft = Math.max(1, endMillis - System.currentTimeMillis());

            String formattedEnd = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(Config.getGiveawayEndtimeInMilli(guildID)));

            Core.info(guild.getName() + " | Active giveaway detected, ends on " + formattedEnd + ".");

            rollGiveawayFuture = API.getExecutorService().schedule(() -> rollGiveaway(guild), timeLeft, TimeUnit.MILLISECONDS);
        });
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getName().equals("giveaway")) return;
        Guild guild = event.getGuild();
        String guildID = guild.getId();
        String subCmd = event.getSubcommandName();


        if ("info".equals(subCmd)) {
            if (rollGiveawayFuture == null || rollGiveawayFuture.isCancelled() || rollGiveawayFuture.isDone()) {
                event.reply("Kein Giveaway gestartet!").setEphemeral(true).queue();
                return;
            }

            Set<String> participants = Config.getGiveawayParticipantSet(guildID);
            Set<String> eligibleUsers = Config.getEligibleUsersForGiveawaySet(guildID);
            long endMillis = Config.getGiveawayEndtimeInMilli(guildID);


            String endTimeFormatted = "<t:" + (endMillis / 1000L) + ":f>";
            String participantsList = participants.isEmpty() ? "Keine Teilnehmer" :
                    participants.stream()
                            .map(id -> "<@" + id + ">")
                            .collect(Collectors.joining(", "));
            String eligibleList = eligibleUsers.isEmpty() ? "Keine berechtigten User" :
                    eligibleUsers.stream()
                            .map(id -> "<@" + id + ">")
                            .collect(Collectors.joining(", "));

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Giveaway-Info")
                    .addField("Teilnehmer (" + participants.size() + ")", participantsList, false)
                    .addField("Berechtigte User (" + eligibleUsers.size() + ")",
                            eligibleList + "\n\n*Nutzer, die jemanden eingeladen haben, dürfen teilnehmen*", false)
                    .addField("Endzeit", endTimeFormatted, false)
                    .setColor(Color.ORANGE);

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }


        if ("cancel".equals(subCmd)) {
            if (delayedGiveawayFuture != null) delayedGiveawayFuture.cancel(true);
            if (rollGiveawayFuture != null) rollGiveawayFuture.cancel(true);
            Config.setGiveawayEndtime(guildID, -1);
            event.reply("Giveaway wurde gecancelt!").setEphemeral(true).queue();
            discordAPI.getDiscordLogChannel(event.getGuild().getId()).sendMessage("Giveaway wurde gecancelt!").queue();
            return;
        }


        if (!"start".equals(subCmd)) return;

        int duration = getIntOption(event, "dauer", "Bitte gib die Dauer des Giveaways in Stunden an!", "Ungültige Dauer! Bitte gib eine Zahl zwischen 1 und 336 an.");
        if (duration == -1) return;

        int delay = getIntOption(event, "delay", null, "Ungültige Delay! Bitte gib eine Zahl zwischen 1 und 336 an.");
        if (delayedGiveawayFuture != null) delayedGiveawayFuture.cancel(true);

        String startMessage;
        if (delay > 0) {
            Instant startTime = Instant.now().plus(delay, COMMAND_TIMEUNIT);
            long unixTimestamp = startTime.getEpochSecond();
            String dynamicTime = "<t:" + unixTimestamp + ":F>";

            delayedGiveawayFuture = API.getExecutorService().schedule(
                    () -> startGiveaway(guild, event.getChannel(), duration),
                    ChronoUnit.MILLIS.between(Instant.now(), startTime),
                    TimeUnit.MILLISECONDS
            );

            startMessage = "Giveaway start am: " + dynamicTime + " (Duration: " + duration + " " + COMMAND_TIMEUNIT.name() + " )";
        } else {
            startMessage = "Giveaway gestartet! (Duration: " + duration + " " + COMMAND_TIMEUNIT.name() + " )";
            startGiveaway(guild, event.getChannel(), duration);
        }

        discordAPI.getDiscordLogChannel(event.getGuild().getId()).sendMessage(startMessage).queue();
        event.reply(startMessage).setEphemeral(true).queue();
    }

    private void startGiveaway(Guild guild, MessageChannelUnion channel, int dauer) {
        String guildID = guild.getId();

        if (rollGiveawayFuture != null) {
            rollGiveawayFuture.cancel(false);
        }

        Config.resetLastGiveaway(guildID);

        Instant endTime = Instant.now().plus(dauer, COMMAND_TIMEUNIT);
        long unixTimestamp = endTime.getEpochSecond();
        String dynamicTime = "<t:" + unixTimestamp + ":F>";

        Config.setGiveawayEndtime(guildID, endTime.toEpochMilli());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎉 Giveaway!")
                .setDescription(
                        "🎉 **Willkommen zum Giveaway!** 🎉\n\n" +
                                "Um an diesem Gewinnspiel teilzunehmen, drücke einfach auf den Button unten.\n\n" +
                                "💰  **Preis**:\n" +
                                " 20 € nach Wahl – ob PayPal, Gutschein oder worauf auch immer du Bock hast.\n\n" +
                                "🔗 **Hinweis**:\n" +
                                "Um teilnahmeberechtigt zu sein, musst du mindestens eine weitere Person auf diesen Server eingeladen haben.\n" +
                                "Nur wer diese Voraussetzung erfüllt, kann teilnehmen!\n\n" +
                                "📅 Ablaufdatum: " + dynamicTime + "\n\n" +
                                "👥 Teilnehmer: **0** \n\n" +
                                "Viel Glück an alle Teilnehmer!"
                )
                .setColor(Color.MAGENTA);

        channel.sendMessage("@everyone").setEmbeds(embed.build()).addActionRow(Button.primary("giveaway_join", "Mitmachen!")).queue();

        rollGiveawayFuture = API.getExecutorService().schedule(() -> rollGiveaway(guild), endTime.toEpochMilli() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!"giveaway_join".equals(event.getButton().getId())) return;
        if (event.getMember() == null) return;
        String guildID = event.getGuild().getId();


        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("Als Admin kannst du nicht an diesem Giveaway teilnehmen.").setEphemeral(true).queue();
            return;
        }

        if (Config.getGiveawayEndtimeInMilli(guildID) == -1 || Instant.now().isAfter(Instant.ofEpochMilli(Config.getGiveawayEndtimeInMilli(guildID)))) {
            event.reply("Dieses Giveaway ist bereits abgelaufen oder ungültig.").setEphemeral(true).queue();
            return;
        }


        if (!Config.getEligibleUsersForGiveawaySet(guildID).contains(event.getUser().getId())) {
            event.reply("Du musst mindestens einen User auf den Server einladen um am Giveaway teilnehmen zu können.").setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();
        if (!Config.addGiveawayParticipant(guildID, userId)) {
            event.reply("Du nimmst bereits am Giveaway teil!").setEphemeral(true).queue();
        } else {
            event.reply("Du bist jetzt beim Giveaway dabei!").setEphemeral(true).queue();
            updateMessage(event.getGuild(), event.getInteraction().getMessage());
        }
    }

    private void rollGiveaway(Guild guild) {
        String guildID = guild.getId();

        try {
            GuildMessageChannel channel = discordAPI.getJDA().awaitReady().getChannelById(GuildMessageChannel.class, Config.getGiveawayChannelID(guildID));

            if (channel == null) {
                Core.severe(guild.getName() + " | Giveaway channel(" + Config.getGiveawayChannelID(guildID) + ") could not be found!");
                return;
            }

            if (Config.getGiveawayParticipantSet(guildID).isEmpty()) {
                channel.sendMessage("Das Giveaway ist beendet, aber es gab leider keine Teilnehmer. 😢").queue();
            } else {
                List<String> userList = Config.getGiveawayParticipantSet(guildID).stream().filter(userId -> discordAPI.getGuild(guildID).getMemberById(userId) != null).toList();

                Core.info(guild.getName() + " | RollGiveaway with user(" + userList.size() + ") -> " + userList);
                String winnerId = userList.get(new Random().nextInt(userList.size()));
                Core.info(guild.getName() + " | Winner -> " + discordAPI.getGuild(guildID).getMemberById(winnerId));

                channel.sendMessage("🎉 **Das Giveaway ist beendet!** 🎉\n" +
                        "Glückwunsch an <@" + winnerId + ">! Du hast gewonnen!\n" +
                        "Bitte melde dich bei der Orga für deinen Gewinn!\n" +
                        "@everyone").queue();
            }

            Config.setGiveawayEndtime(guildID, -1);

        } catch (Exception e) {
            Core.severe(e.getMessage(), e);
        }
    }


    private void updateMessage(Guild guild, Message message) {
        MessageEmbed oldEmbed = message.getEmbeds().getFirst();

        if (oldEmbed == null || oldEmbed.getDescription() == null) {
            Core.warn(guild.getName() + " | Failed to update Giveaway-Message Participant-Count!");
            return;
        }

        String newDescription = oldEmbed.getDescription()
                .replaceAll("(👥 Teilnehmer: )\\*\\*\\d+\\*\\*", "$1**" + Config.getGiveawayParticipantSet(guild.getId()).size() + "**");
        EmbedBuilder newEmbed = new EmbedBuilder(oldEmbed).setDescription(newDescription);
        message.editMessageEmbeds(newEmbed.build()).queue();
    }

    private int getIntOption(SlashCommandInteractionEvent event, String name, String nullMsg, String invalidMsg) {
        OptionMapping opt = event.getOption(name);
        if (opt == null) {
            if (nullMsg != null) event.reply(nullMsg).setEphemeral(true).queue();
            return -1;
        }
        int val = opt.getAsInt();
        if (val < 1 || val > 336) {
            event.reply(invalidMsg).setEphemeral(true).queue();
            return -1;
        }
        return val;
    }
}