package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class CloseCommand extends ListenerAdapter
{

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("close")) return;

        if(!(event.getChannel() instanceof TextChannel) || ((TextChannel) event.getChannel()).getParentCategory() == null || !((TextChannel) event.getChannel()).getParentCategory().getName().equals("Tickets")){
            event.replyEmbeds(MessageUtils.getDefaultEmbed().setTitle("Fehler").setDescription("Das ist kein Ticket").build()).setEphemeral(true).queue();
            return;
        }

        ((TextChannel) event.getChannel()).getMemberPermissionOverrides().forEach(user ->
                Objects.requireNonNull(user.getMember()).getUser().openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                .setAuthor("GiantNetwork")
                .setColor(Color.GREEN)
                .setTitle("Vielen Dank, dass Sie den Support kontaktiert haben!")
                .setDescription("Ihr Fall wurde aufgrund einer Lösung geschlossen. \nWenn Sie ein anderes Problem haben, können Sie möglicherweise ein weiteres Ticket eröffnen.")
                .build())).queue());

        event.getChannel().delete().queue();
    }
}
