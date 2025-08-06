package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getGuild() != null && !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        Guild guild = event.getGuild();
        StringBuilder builder = new StringBuilder();
        builder.append(event.getSubcommandName() == null ? "" : " " + event.getSubcommandName());
        event.getOptions().forEach(optionMapping -> builder
                .append(" ")
                .append(optionMapping.getName())
                .append(":")
                .append(optionMapping.getAsString()));

        Core.info((event.getGuild() != null ? event.getGuild().getName() + " | " : "") + "User '" + event.getUser().getName() + "' run command '" + event.getName() + builder + "'" + (event.getGuild() == null ? " via private message" : ""));
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (event.getGuild() != null && !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        Core.info((event.getGuild() != null ? event.getGuild().getName() + " | " : "") + "User '" + event.getUser().getName() + "' run app '" + event.getName() + "'");
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getGuild() != null && !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        Core.info((event.getGuild() != null ? event.getGuild().getName() + " | " : "") + "User '" + event.getUser().getName() + "' run app '" + event.getName() + "'");
    }
}
