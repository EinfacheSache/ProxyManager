package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ReadyListener implements EventListener {

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {

            Thread.currentThread().setName("DISCORD");

            JDA jda = event.getJDA();
            List<Guild> joinedGuilds = jda.getGuilds();
            Guild guild = jda.getGuildById(Config.getGuildID());

            Core.info("DiscordAPI is ready!");
            Core.info("Logged in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());
            Core.info("Connected to " + joinedGuilds.size() + " Guilds : " + Arrays.toString(joinedGuilds.toArray()));

            if (guild == null) {
                Core.severe("Running for Guild : NULL");
                return;
            }

            Core.info("Running for Guild : " + guild.getName() + "(MemberCount=" + guild.getMembers().size() + ")");
            Core.info("Load retrieve invites from " + guild.getName());

            guild.retrieveInvites().queue(invites -> invites.forEach(invite -> MemberJoinGuildListener.getInviteUses().put(invite.getCode(), invite.getUses())));
        }
    }
}
