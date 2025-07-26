package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public class ManagerCommand extends ListenerAdapter {

    private final DiscordAPI discordAPI;

    public ManagerCommand(DiscordAPI discordAPI) {
        this.discordAPI = discordAPI;
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("manager")) return;

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();
        Guild guild = event.getGuild();

        switch (Objects.requireNonNull(event.getSubcommandName())) {

            case "restart" -> {
                embedBuilder.setDescription("Restart wird ausgeführt!");
                Core.shutdown();
                Core.run();
            }

            case "reload-commands" -> {
                Core.getDiscordAPI().loadDiscordCommands();
                embedBuilder.setDescription("Commands wurden reloaded!");
                Core.info("Updating Discord slash commands");
            }

            case "close-all-tickets" -> {
                discordAPI.getTicketCategory().getChannels().forEach(c -> c.delete().queue());
                embedBuilder.setDescription("Alle Tickets gelöscht");
                Core.info("All tickets deleted");
            }

            case "ticket-setup" -> {
                embedBuilder.setDescription("Ticket message wurde erstellt!");
                EmbedBuilder ticket = MessageUtils.getDefaultEmbed()
                        .setColor(Color.BLUE)
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
                        .build();

                event.getChannel().sendMessageEmbeds(ticket.build()).addActionRow(menu).queue();

                if (guild == null) {
                    Core.severe("Guild was null! ID(" + Config.getGuildID() + ")");
                    return;
                }


                Role staffRole = discordAPI.getStaffRole();
                Category ticketCategory = discordAPI.getTicketCategory();

                ticketCategory.getManager()
                        .putRolePermissionOverride(staffRole.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                        .putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();

                Core.info("Ticketbot has been successfully setup");
            }
        }
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
