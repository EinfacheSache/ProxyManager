package de.cubeattack.proxymanager.discord.listener;

import de.cubeattack.proxymanager.core.Core;
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
            JDA jda = event.getJDA();
            List<Guild> guilds = jda.getGuilds();
            Core.info("DiscordAPI is ready!");
            Core.info("Logged in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());
            Core.info("Connected to " + guilds.size() + " Guilds : " + Arrays.toString(guilds.toArray()));
        }
    }
}
