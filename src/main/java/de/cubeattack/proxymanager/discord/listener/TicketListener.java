package de.cubeattack.proxymanager.discord.listener;

import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public class TicketListener extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (event.getComponentId().equals("ticket:select")) {

            TextInput body = TextInput.create("body", "Bitte beschreibe dein Problem", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Beschreiben Sie Ihr Problem/Ihre Anfrage")
                    .setMinLength(24)
                    .setMaxLength(2048)
                    .build();

            Modal modal = Modal.create("ticket:describe:" + event.getSelectedOptions().get(0).getValue(), event.getSelectedOptions().get(0).getLabel())
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
            event.reply("Danke für ihre Anfrage").setEphemeral(true).queue();

            Guild guild = event.getGuild();
            String body = Objects.requireNonNull(event.getValue("body")).getAsString();

            String teamID = Config.getTeamRoleID();
            if (teamID.isEmpty() || guild.getRoleById(teamID) == null) {
                teamID = guild.createRole().setName("✦Team✦").setColor(Color.GREEN).complete().getId();
                Config.setTeamRoleID(teamID);
            }

            if (Config.getCategoryID().isEmpty() || guild.getCategoryById(Config.getCategoryID()) == null) {
                Category category = guild.createCategory("[Tickets]").complete();
                category.getManager()
                        .putRolePermissionOverride(Long.parseLong(teamID), EnumSet.of(Permission.VIEW_CHANNEL), null)
                        .putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
                Config.setCategoryID(category.getId());
            }

            TextChannel channel = Objects.requireNonNull(guild.getCategoryById(Config.getCategoryID())).createTextChannel(event.getModalId().split(":")[2] + "-" + event.getUser().getName()).complete();

            channel.getManager().putMemberPermissionOverride(event.getUser().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null).queue();

            channel.sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                    .setDescription("# Willkommen bei deinem Ticket " + "<@" + event.getUser().getId() + ">\n" +
                            "Es wird Ihnen schnellstmöglich ein Helfer zur Seite stehen.\n" +
                            "Bitte pingen Sie unsere Team nicht selbst an, sondern nur in Notsituationen.\n" +
                            "(Die Nichtbeachtung dieser Regel führt zu einem Timeout/Ban)"
                    ).setColor(Color.GREEN).build())
                    .addActionRow(Button.danger("delete_ticket", "\uD83D\uDDD1\uFE0F Ticket schließen"))
                    .queue();

            channel.sendMessage("<@&" + teamID + ">").queue();
            channel.sendMessageEmbeds(MessageUtils.getDefaultEmbed()
                    .setDescription("### Erste Anfrage/Problem:\n" + body).setColor(Color.GREEN).build()).queue();
        }
    }
}