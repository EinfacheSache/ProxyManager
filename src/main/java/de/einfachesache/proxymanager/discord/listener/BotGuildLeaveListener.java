package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Core;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotGuildLeaveListener extends ListenerAdapter {

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Core.getDiscordAPI().removeGuild(event.getGuild().getId());
    }
}
