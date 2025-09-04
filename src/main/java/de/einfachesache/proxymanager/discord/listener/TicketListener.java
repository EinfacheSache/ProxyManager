package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.modals.Modal;

import java.awt.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

public class TicketListener extends ListenerAdapter {

    private static DiscordAPI discordAPI;

    public TicketListener(DiscordAPI discordAPI) {
        TicketListener.discordAPI = discordAPI;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (event.getComponentId().equals("ticket:select")) {

            TextInput body = TextInput.create("body", "Bitte beschreibe dein Problem", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Beschreiben Sie Ihr Problem/Ihre Anfrage")
                    .setMinLength(24)
                    .setMaxLength(4096)
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
        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (event.getModalId().startsWith("ticket:describe")) {
            event.reply("Danke für Ihre Anfrage").setEphemeral(true).queue();

            String body = Objects.requireNonNull(event.getValue("body")).getAsString();
            String type = event.getModalId().split(":")[2];
            boolean adminOnly = type.equalsIgnoreCase("partnership");

            createTicket(event.getGuild().getId(), type, event.getUser(), body, adminOnly);
        }
    }

    public static void createTicket(String guildID, String category, User user, String initialRequest, boolean adminOnly) {
        Role staffRole = discordAPI.getStaffRole(guildID);
        Category ticketCategory = discordAPI.getTicketCategory(guildID);
        TextChannel channel = ticketCategory.createTextChannel(category + "-" + user.getName()).complete();
        TextChannelManager manager = channel.getManager();

        manager.putMemberPermissionOverride(user.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), Collections.emptySet()).queue();

        if (adminOnly) {
            manager.removePermissionOverride(staffRole).queue();
        } else {
            manager.putRolePermissionOverride(staffRole.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), Collections.emptySet()).queue();
        }

        channel.sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                        .setDescription("# Willkommen bei deinem Ticket " + "<@" + user.getId() + ">\n" +
                                "Es wird Ihnen schnellstmöglich ein Helfer zur Seite stehen.\n" +
                                "Bitte pingen Sie unsere Team nicht selbst an, sondern nur in Notsituationen.\n" +
                                "(Die Nichtbeachtung dieser Regel führt zu einem Timeout/Ban)"
                        ).setColor(Color.GREEN).build())
                .addComponents(ActionRow.of(Button.danger("delete_ticket", "\uD83D\uDDD1️ Ticket schließen")))
                .queue();

        channel.sendMessage("<@&" + staffRole.getId() + ">")
                .setEmbeds(MessageUtils.getDefaultEmbed().setDescription("### Erste Anfrage/Problem:\n" + initialRequest).setColor(Color.GREEN).build())
                .queue();
    }

    public static void createBugReportTicket(String guildID, String userID, String report) {
        User user = Core.getDiscordAPI().getJDA().getUserById(userID);

        if (Config.getDiscordServerProfile(guildID).getGuildId() == null) {
            Core.warn("Can't create bugreport. guild with ID " + guildID + " is null");
            return;
        }

        if (user == null) {
            Core.warn("Can't create bugreport. user with ID " + userID + " is null");
            return;
        }

        createTicket(guildID, "report", user, report, false);
    }
}