package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public class TicketListener extends ListenerAdapter {

    private final DiscordAPI discordAPI;

    public TicketListener(DiscordAPI discordAPI) {
        this.discordAPI = discordAPI;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (event.getComponentId().equals("ticket:select")) {

            TextInput body = TextInput.create("body", "Bitte beschreibe dein Problem", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Beschreiben Sie Ihr Problem/Ihre Anfrage")
                    .setMinLength(24)
                    .setMaxLength(2048)
                    .build();

            Modal modal = Modal.create("ticket:describe:" + event.getSelectedOptions().getFirst().getValue(), event.getSelectedOptions().getFirst().getLabel())
                    .addComponents(ActionRow.of(body))
                    .build();

            event.replyModal(modal).queue();
            event.editSelectMenu(event.getSelectMenu().createCopy().build()).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getGuild() == null) return;
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (event.getModalId().startsWith("ticket:describe")) {
            event.reply("Danke für Ihre Anfrage").setEphemeral(true).queue();

            String body = Objects.requireNonNull(event.getValue("body")).getAsString();

            Role staffRoleID = discordAPI.getStaffRole();
            Category ticketCategory = discordAPI.getTicketCategory();
            TextChannel channel = ticketCategory.createTextChannel(event.getModalId().split(":")[2] + "-" + event.getUser().getName()).complete();

            channel.getManager().putMemberPermissionOverride(event.getUser().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null).queue();

            channel.sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                            .setDescription("# Willkommen bei deinem Ticket " + "<@" + event.getUser().getId() + ">\n" +
                                    "Es wird Ihnen schnellstmöglich ein Helfer zur Seite stehen.\n" +
                                    "Bitte pingen Sie unsere Team nicht selbst an, sondern nur in Notsituationen.\n" +
                                    "(Die Nichtbeachtung dieser Regel führt zu einem Timeout/Ban)"
                            ).setColor(Color.GREEN).build())
                    .addActionRow(Button.danger("delete_ticket", "\uD83D\uDDD1️ Ticket schließen"))
                    .queue();

            channel.sendMessage("<@&" + staffRoleID.getId() + ">")
                    .setEmbeds(MessageUtils.getDefaultEmbed().setDescription("### Erste Anfrage/Problem:\n" + body).setColor(Color.GREEN).build()).queue();
        }
    }
}