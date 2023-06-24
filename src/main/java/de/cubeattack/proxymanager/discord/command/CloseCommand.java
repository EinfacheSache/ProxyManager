package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.proxymanager.core.Core;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class CloseCommand extends ListenerAdapter
{

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("close")) return;
        event.getChannel().delete().queue();
    }
}
