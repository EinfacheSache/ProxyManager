package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReadyListener implements EventListener {

    private final DiscordAPI discordAPI;

    public ReadyListener(DiscordAPI discordAPI) {
        this.discordAPI = discordAPI;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {

            JDA jda = event.getJDA();
            Thread.currentThread().setName("DISCORD");
            Config.getGuildIDs().forEach(id -> discordAPI.getGuilds().put(id, jda.getGuildById(id)));

            List<Guild> joinedGuilds = jda.getGuilds();
            List<Guild> guilds = discordAPI.getGuilds().values().stream().toList();

            Core.info("DiscordAPI is ready!");
            Core.info("Logged in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());
            Core.info("Connected to " + joinedGuilds.size() + " Guilds : " + Arrays.toString(joinedGuilds.toArray()));

            if (guilds.isEmpty()) {
                Core.severe("Running for Guild : NULL");
                return;
            }

            Core.info("Running for Guilds: " + guilds.stream()
                    .map(g -> g.getName() + "(Members=" + g.getMembers().size() + ")")
                    .collect(Collectors.joining(", ")));


            guilds.forEach(guild ->
                    guild.retrieveInvites().queue(invites ->
                            invites.forEach(invite ->
                                    MemberJoinGuildListener.getInviteUses().put(invite.getCode(), invite.getUses()))));
        }
    }
}
