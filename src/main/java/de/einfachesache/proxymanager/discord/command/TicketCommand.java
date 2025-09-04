package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public class TicketCommand extends ListenerAdapter {

    private final DiscordAPI discordAPI;

    public TicketCommand(DiscordAPI discordAPI) {
        this.discordAPI = discordAPI;
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getGuild() == null || !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getName().equalsIgnoreCase("ticket")) return;

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();
        Guild guild = event.getGuild();

        switch (Objects.requireNonNull(event.getSubcommandName())) {

            case "close-all" -> {
                discordAPI.getTicketCategory(guild.getId()).getChannels().forEach(c -> c.delete().queue());
                embedBuilder.setDescription("Alle Tickets gelöscht");
                Core.info(guild.getName() + " | All tickets deleted");
            }

            case "setup" -> {
                embedBuilder.setDescription("Ticket message wurde erstellt!");
                EmbedBuilder ticket = MessageUtils.getDefaultEmbed()
                        .setDescription("""
                                # Erstelle ein Ticket
                                 Derzeit werden Tickets nur auf Deutsch oder Englisch beantwortet.
                                 Alle anderen Tickets werden sofort geschlossen. Sie können mit Antworten in max. 12 Stunden rechnen.""");
                SelectMenu menu = StringSelectMenu.create("ticket:select")
                        .setPlaceholder("Wähle ein Thema")
                        .addOption("Generell", "general", "Für generelle Fragen und Probleme")
                        .addOption("Report", "report", "Für Spieler- und Bug-Reports")
                        .addOption("Käufe", "purchases", "Für Probleme oder Fragen bezüglich Käufen z.B. Ränge")
                        .addOption("Bewerbung", "application", "Bewerbe dich als Builder, Supporter, Developer...")
                        .addOption("Partnerschaft", "partnership", "Für Anfragen zu Partnerschaften und Kooperationen")
                        .build();

                event.getChannel().sendMessageEmbeds(ticket.build()).addComponents(ActionRow.of(menu)).queue();

                Role staffRole = discordAPI.getStaffRole(guild.getId());
                Category ticketCategory = discordAPI.getTicketCategory(guild.getId());

                ticketCategory.getManager()
                        .putRolePermissionOverride(staffRole.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                        .putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();

                Core.info(guild.getName() + " | Ticketbot has been successfully setup");
            }
        }
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
