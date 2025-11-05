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

import java.util.*;
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
            discordAPI.addGuilds(Config.getGuildIDs().stream()
                    .map(jda::getGuildById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Guild::getId, g -> g)));

            List<Guild> joinedGuilds = jda.getGuilds();
            Collection<Guild> connectedGuilds = discordAPI.getGuilds().values();

            Core.info("DiscordAPI is ready!");
            Core.info("Logged in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());
            Core.info("Connected to " + joinedGuilds.size() + " Guilds: " +
                    joinedGuilds.stream().map(g -> g.getName() + "(ID=" + g.getId() + ")")
                    .collect(Collectors.joining(", ")));

            if (connectedGuilds.isEmpty()) {
                Core.severe("Running for Guild : NULL");
                return;
            }

            Core.info("Running for " + connectedGuilds.size() + " Guilds: " + connectedGuilds.stream()
                    .map(g -> g.getName() + "(Members=" + g.getMembers().size() + ")")
                    .collect(Collectors.joining(", ")));

            Core.info("Running for " + connectedGuilds.stream().mapToLong(Guild::getMemberCount).sum() + " Users");


            connectedGuilds.forEach(guild ->
                    guild.retrieveInvites().queue(invites ->
                            invites.forEach(invite ->
                                    MemberJoinGuildListener.getInviteUses().put(invite.getCode(), invite.getUses()))));
        }
    }
}
