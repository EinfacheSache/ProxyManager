package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class MemberLeaveGuildListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (!Config.getGuildIDs().contains(event.getGuild().getId())) return;

        Guild guild = event.getGuild();
        User user = event.getUser();

        if (Core.getDiscordAPI().getGuilds().values().stream().noneMatch(otherGuild -> otherGuild.getMemberById(event.getUser().getId()) != null)) {
            Config.removeFromWhitelistByUser(guild.getId(), user.getId());
        }

        String leaveChannelId = Config.getDiscordServerProfile(guild.getId()).getLeaveChannelId();
        if (Objects.equals(leaveChannelId, "-1")) {
            return;
        }

        TextChannel channel = guild.getChannelById(TextChannel.class, leaveChannelId);
        if (channel == null) {
            Core.info(guild.getName() + " | ❌ Leave channel wurde für (" + guild.getName() + ") nicht gefunden: " + leaveChannelId);
            return;
        }

        channel.sendMessage("↩️ <@" + user.getId() + "> (" + user.getName() + ") hat den Server verlassen!").queue();
    }
}