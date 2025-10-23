package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.velocity.listener.LoginAccessControlListener;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberLeaveGuildListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (!Config.getGuildIDs().contains(event.getGuild().getId())) return;

        String minecraftName = Config.getWhitelistedPlayers().get(event.getUser().getId());

        if(minecraftName == null)
            return;

        if(Config.removeFromWhitelistByPlayer(minecraftName) && Core.isMinecraftServer()){
            LoginAccessControlListener.sendLimboOnWhitelistRemove(minecraftName);
        }
    }
}