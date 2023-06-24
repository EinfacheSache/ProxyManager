package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public class ManagerCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {

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
                Core.getDiscordAPI().loadCommands();
                embedBuilder.setDescription("Commands wurden reloaded!");
                Core.info("Updating Discord slash commands\n");
            }

            case "closetickets" -> {
                Objects.requireNonNull(event.getGuild()).getCategoriesByName("Tickets", false).get(0).getChannels().forEach(c -> c.delete().queue());
                embedBuilder.setDescription("Alle Tickets gelöscht");
            }

            case "ticketsetup" -> {
                embedBuilder.setDescription("Ticket message wurde erstellt!");
                EmbedBuilder ticket = MessageUtils.getDefaultEmbed()
                        .setColor(Color.BLUE)
                        .setDescription("""
                                # Ticket
                                 Derzeit werden Tickets nur auf Englisch oder Deutsch beantwortet.
                                 Alle anderen Tickets werden sofort geschlossen. Sie können mit Antworten in max. 12 Stunden.""");
                SelectMenu menu = StringSelectMenu.create("ticket:select")
                        .setPlaceholder("Wähle ein Thema")
                        .addOption("Generell", "general", "Für generelle Fragen oder Probleme")
                        .addOption("Report", "report", "Für Spieler und Bugs Reports")
                        .addOption("Bewerbung", "application", "Bewerbe dich als Builder, Supporter, Developer...")
                        .build();

                event.getChannel().sendMessageEmbeds(ticket.build()).addActionRow(menu).queue();

                if(guild != null && guild.getCategoriesByName("Tickets", false).isEmpty()){
                    long teamID;
                    if(guild.getRolesByName("✦Team✦", true).isEmpty()){
                        teamID = guild.createRole().setName("✦Team✦").setColor(Color.RED).complete().getIdLong();
                    }else {
                        teamID = guild.getRolesByName("✦Team✦", true).get(0).getIdLong();
                    }

                    guild.createCategory("Tickets").complete().getManager()
                            .putRolePermissionOverride(teamID, EnumSet.of(Permission.VIEW_CHANNEL), null)
                            .putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
                }
            }
        }
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
