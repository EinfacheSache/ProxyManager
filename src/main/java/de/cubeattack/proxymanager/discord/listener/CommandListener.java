package de.cubeattack.proxymanager.discord.listener;

import de.cubeattack.proxymanager.core.Core;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.isFromGuild() & !Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        StringBuilder builder = new StringBuilder();
        builder.append(event.getSubcommandName() == null ? "" : " " + event.getSubcommandName());
        event.getOptions().forEach(optionMapping -> builder
                .append(" ")
                .append(optionMapping.getName())
                .append(":")
                .append(optionMapping.getAsString()));

        Core.info("User '" + event.getUser().getName() + "' run command '" + event.getName() + builder + "'");
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (event.isFromGuild() & !Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        Core.info("User '" + event.getUser().getName() + "' run app '" + event.getName() + "'");
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.isFromGuild() & !Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        Core.info("User '" + event.getUser().getName() + "' run app '" + event.getName() + "'");
    }
}
