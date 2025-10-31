package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberLeaveGuildListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (!Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (Core.getDiscordAPI().getGuilds().values().stream().anyMatch(guild -> guild.getMemberById(event.getUser().getId()) != null)) return;

        Config.removeFromWhitelistByUser(event.getGuild().getId(), event.getUser().getId());
    }
}