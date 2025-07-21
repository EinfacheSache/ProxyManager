package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.api.API;
import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.DiscordAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GiveawayCommand extends ListenerAdapter {

    private final DiscordAPI discordAPI;

    private final ChronoUnit COMMAND_TIMEUNIT = ChronoUnit.HOURS;
    private ScheduledFuture<?> rollGiveawayFuture;
    private ScheduledFuture<?> delayedGiveawayFuture;

    public GiveawayCommand(DiscordAPI discordAPI) {
        this.discordAPI = discordAPI;

        long endMillis = Config.getGiveawayEndtimeInMilli();
        if (endMillis == -1) return;

        long timeLeft = Math.max(1, endMillis - System.currentTimeMillis());

        String formattedEnd = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now().plus(timeLeft, TimeUnit.MILLISECONDS.toChronoUnit()));

        Core.info("Active giveaway detected, ends on " + formattedEnd + ".");

        rollGiveawayFuture = API.getExecutorService().schedule(this::rollGiveaway, timeLeft, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equals("giveaway")) return;

        String subCmd = event.getSubcommandName();

        if ("cancel".equals(subCmd)) {
            if (delayedGiveawayFuture != null) delayedGiveawayFuture.cancel(true);
            if (rollGiveawayFuture != null) rollGiveawayFuture.cancel(true);
            Config.resetLastGiveaway();
            event.reply("Giveaway wurde gecancelt!").setEphemeral(true).queue();
            discordAPI.getLogChannel().sendMessage("Giveaway wurde gecancelt!").queue();
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
                    () -> startGiveaway(event.getChannel(), duration),
                    ChronoUnit.MILLIS.between(Instant.now(), startTime),
                    TimeUnit.MILLISECONDS
            );

            startMessage = "Giveaway start am: " + dynamicTime + " (Duration: " + duration + " " + COMMAND_TIMEUNIT.name() + " )";
        } else {
            startMessage = "Giveaway gestartet! (Duration: " + duration + " " + COMMAND_TIMEUNIT.name() + " )";
            startGiveaway(event.getChannel(), duration);
        }

        discordAPI.getLogChannel().sendMessage(startMessage).queue();
        event.reply(startMessage).setEphemeral(true).queue();
    }

    private void startGiveaway(MessageChannelUnion channel, int dauer) {

        if (rollGiveawayFuture != null) {
            rollGiveawayFuture.cancel(false);
        }

        Config.resetLastGiveaway();

        Instant endTime = Instant.now().plus(dauer, COMMAND_TIMEUNIT);
        long unixTimestamp = endTime.getEpochSecond();
        String dynamicTime = "<t:" + unixTimestamp + ":F>";

        Config.setGiveawayEndtime(endTime.toEpochMilli());

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

        rollGiveawayFuture = API.getExecutorService().schedule(this::rollGiveaway, endTime.toEpochMilli() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!"giveaway_join".equals(event.getButton().getId())) return;

        if (Config.getGiveawayEndtimeInMilli() == -1 || Instant.now().isAfter(Instant.ofEpochMilli(Config.getGiveawayEndtimeInMilli()))) {
            event.reply("Dieses Giveaway ist bereits abgelaufen oder ungültig.").setEphemeral(true).queue();
            return;
        }

        if (!Config.getEligibleUsersForGiveawaySet().contains(event.getUser().getId())) {
            event.reply("Du musst mindestens einen User auf den Server einladen um am Giveaway teilnehmen zu können.").setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();
        if (!Config.addGiveawayParticipant(userId)) {
            event.reply("Du nimmst bereits am Giveaway teil!").setEphemeral(true).queue();
        } else {
            event.reply("Du bist jetzt beim Giveaway dabei!").setEphemeral(true).queue();
            updateMessage(event.getInteraction().getMessage());
        }
    }

    private void rollGiveaway() {
        try {
            TextChannel channel = discordAPI.getJDA().awaitReady().getTextChannelById(Config.getGiveawayChannelID());

            if (channel == null) {
                Core.warn("Giveaway channel could not be found!");
                return;
            }

            if (Config.getGiveawayParticipantSet().isEmpty()) {
                channel.sendMessage("Das Giveaway ist beendet, aber es gab leider keine Teilnehmer. 😢").queue();
            } else {
                List<String> userList = Config.getGiveawayParticipantSet().stream().toList();
                String winnerId = userList.get(new Random().nextInt(userList.size()));
                channel.sendMessage("🎉 **Das Giveaway ist beendet!** 🎉\n" +
                        "Glückwunsch an <@" + winnerId + ">! Du hast gewonnen!\n" +
                        "Bitte melde dich bei der Orga für deinen Gewinn!").queue();
            }

            Config.setGiveawayEndtime(-1);

        } catch (InterruptedException e) {
            Core.severe(e.getMessage(), e);
        }
    }


    private void updateMessage(Message message) {
        MessageEmbed oldEmbed = message.getEmbeds().get(0);

        if (oldEmbed == null || oldEmbed.getDescription() == null) {
            Core.warn("Failed to update Giveaway-Message Participant-Count!");
            return;
        }

        String newDescription = oldEmbed.getDescription()
                .replaceAll("(👥 Teilnehmer: )\\*\\*\\d+\\*\\*", "$1**" + Config.getGiveawayParticipantSet().size() + "**");
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