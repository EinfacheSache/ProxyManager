package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;

import java.util.Objects;

public class CloseCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getName().equalsIgnoreCase("close")) return;
        if (!(event.getChannel() instanceof TextChannel channel)) return;
        String guildID = event.getGuild().getId();

        if (channel.getParentCategory() == null || !channel.getParentCategory().getId().equals(Config.getTicketsCategoryID(guildID))) {
            event.replyEmbeds(MessageUtils.getErrorEmbed().setTitle("Fehler").setDescription("Das ist kein Ticket").build()).setEphemeral(true).queue();
            return;
        }

        closeTicket(guildID, channel, Objects.requireNonNull(event.getMember()),
                event.getOptionsByName("reason").isEmpty() ?
                        "einer Lösung" : event.getOptionsByName("reason").getFirst().getAsString());
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getComponentId().equals("delete_ticket")) return;

        TextInput body = TextInput.create("body", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Nenne den Grund für das Schließen des Tickets")
                .setMinLength(12)
                .setMaxLength(256)
                .build();

        Modal modal = Modal.create("ticket:delete:" + event.getChannelId(), "Ticket schließen")
                .addComponents(Label.of("Begründung für das Schließen", body))
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (event.getModalId().startsWith("ticket:delete")) {
            event.deferReply().complete();
            String body = Objects.requireNonNull(event.getValue("body")).getAsString();
            closeTicket(event.getGuild().getId(), (TextChannel) event.getChannel(), Objects.requireNonNull(event.getMember()), body);
        }
    }


    private void closeTicket(String guildID, TextChannel channel, Member member, String reason) {
        channel.getMemberPermissionOverrides().stream().filter(mpOverride ->
                !Objects.requireNonNull(mpOverride.getMember()).getUser().isBot()).forEach(mpOverride ->
                Objects.requireNonNull(mpOverride.getMember()).getUser().openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                                .setAuthor(Config.getServerName())
                                .setTitle("Vielen Dank, dass Sie den Support kontaktiert haben!")
                                .setDescription("Ihr Fall wurde aufgrund " + reason + " geschlossen. \nWenn Sie ein anderes Problem haben, können Sie möglicherweise ein weiteres Ticket eröffnen.")
                                .build())).queue());

        Core.getDiscordAPI().getDiscordLogChannel(guildID).sendMessageEmbeds(MessageUtils
                .getDefaultEmbed()
                .setAuthor(Config.getServerName())
                .setTitle("Ticket '" + channel.getName() + "' wurde von " + member.getEffectiveName() + " geschlossen.")
                .setDescription("Users : " + channel.getMemberPermissionOverrides().stream().map(permissionOverride -> Objects.requireNonNull(permissionOverride.getMember()).getEffectiveName()).toList().toString().replace("[", "'").replace("]", "'\nGrund: " + (reason == null ? "Kein Grund angegeben" : reason)))
                .build()).queue();

        channel.delete().queue();
    }
}
