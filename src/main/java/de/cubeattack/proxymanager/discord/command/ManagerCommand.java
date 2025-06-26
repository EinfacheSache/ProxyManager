package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public class ManagerCommand extends ListenerAdapter {

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

            case "reloadcommands" -> {
                Core.getDiscordAPI().loadDiscordCommands();
                embedBuilder.setDescription("Commands wurden reloaded!");
                Core.info("Updating Discord slash commands");
            }

            case "closetickets" -> {
                Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getCategoryById(Config.getCategoryID())).getChannels().forEach(c -> c.delete().queue());
                embedBuilder.setDescription("Alle Tickets gelöscht");
            }

            case "ticketsetup" -> {
                embedBuilder.setDescription("Ticket message wurde erstellt!");
                EmbedBuilder ticket = MessageUtils.getDefaultEmbed()
                        .setColor(Color.BLUE)
                        .setDescription("""
                                # Erstelle ein Ticket
                                 Derzeit werden Tickets nur auf Englisch oder Deutsch beantwortet.
                                 Alle anderen Tickets werden sofort geschlossen. Sie können mit Antworten in max. 12 Stunden.""");
                SelectMenu menu = StringSelectMenu.create("ticket:select")
                        .setPlaceholder("Wähle ein Thema")
                        .addOption("Generell", "general", "Für generelle Fragen und Probleme")
                        .addOption("Report", "report", "Für Spieler und Bug-Reports")
                        .addOption("Käufe", "käufe", "Für Probleme oder Fragen bezüglich Käufen z.B. Ränge")
                        .addOption("Bewerbung", "application", "Bewerbe dich als Builder, Supporter, Developer...")
                        .build();

                event.getChannel().sendMessageEmbeds(ticket.build()).addActionRow(menu).queue();

                if(guild == null){
                    Core.severe("Guild was null! ID");
                    return;
                }

                long teamID = Long.parseLong(Config.getTeamRoleID());
                if (guild.getRoleById(teamID) == null) {
                    teamID = guild.createRole().setName("✦Team✦").setColor(Color.GREEN).complete().getIdLong();
                    Config.setTeamRoleID(String.valueOf(teamID));
                }

                Category ticketCategory = guild.getCategoryById(Config.getCategoryID());

                if (ticketCategory == null) {
                    ticketCategory = guild.createCategory("[Tickets]").complete();
                    Config.setCategoryID(ticketCategory.getId());
                }

                ticketCategory.getManager()
                        .putRolePermissionOverride(teamID, EnumSet.of(Permission.VIEW_CHANNEL), null)
                        .putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
            }
        }
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
