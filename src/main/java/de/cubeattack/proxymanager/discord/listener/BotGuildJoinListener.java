package de.cubeattack.proxymanager.discord.listener;

import de.cubeattack.proxymanager.core.Core;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class BotGuildJoinListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        Core.getDiscordAPI().loadDiscordCommands();
    }
}
