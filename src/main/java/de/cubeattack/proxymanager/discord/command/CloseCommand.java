package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class CloseCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("close")) return;
        if (!(event.getChannel() instanceof TextChannel channel)) return;

        if (channel.getParentCategory() == null || !channel.getParentCategory().getId().equals(Config.getCategoryID())) {
            event.replyEmbeds(MessageUtils.getDefaultEmbed().setTitle("Fehler").setDescription("Das ist kein Ticket").build()).setEphemeral(true).queue();
            return;
        }

        String reason = null;
        if (!event.getOptionsByName("reason").isEmpty()) {
            reason = event.getOptionsByName("reason").get(0).getAsString();
        }

        ticketClosed(channel, Objects.requireNonNull(event.getMember()), reason);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("delete_ticket")) {
            return;
        }
        ticketClosed((TextChannel) event.getChannel(), Objects.requireNonNull(event.getMember()), null);
    }

    private void ticketClosed(TextChannel channel, Member member, String reason) {
        channel.getMemberPermissionOverrides().stream().filter(mpOverride ->
                !Objects.requireNonNull(mpOverride.getMember()).getUser().isBot()).forEach(mpOverride ->
                Objects.requireNonNull(mpOverride.getMember()).getUser().openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                                .setAuthor(Config.getServerName())
                                .setColor(Color.GREEN)
                                .setTitle("Vielen Dank, dass Sie den Support kontaktiert haben!")
                                .setDescription("Ihr Fall wurde aufgrund " + (reason == null ? "einer Lösung" : reason) + " geschlossen. \nWenn Sie ein anderes Problem haben, können Sie möglicherweise ein weiteres Ticket eröffnen.")
                                .build())).queue());

        Core.getDiscordAPI().getLogChannel().sendMessageEmbeds(MessageUtils
                .getDefaultEmbed()
                .setAuthor(Config.getServerName())
                .setColor(Color.GREEN)
                .setTitle("Ticket '" + channel.getName() + "' wurde von " + member.getEffectiveName() + " geschlossen.")
                .setDescription("Users : " + channel.getMemberPermissionOverrides().stream().map(permissionOverride -> Objects.requireNonNull(permissionOverride.getMember()).getEffectiveName()).toList().toString().replace("[", "'").replace("]", "'\nGrund: " + (reason == null ? "Kein Grund angegeben" : reason)))
                .build()).queue();

        channel.delete().queue();
    }
}
