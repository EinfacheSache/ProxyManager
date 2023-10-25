package de.cubeattack.proxymanager.discord.command;

import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class PingCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.isFromGuild() & !Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("ping")) return;
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();
        embedBuilder.setTitle("Der Ping des Bots beträgt ..." +
                "\nDer Ping zum Discord Websocket beträgt ...");
        embedBuilder.setColor(Color.RED);

        Core.getDiscordAPI().getJDA().getGatewayPing();
        long time = System.currentTimeMillis();
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true)
                .flatMap(v ->
                        event.getHook().editOriginalEmbeds(embedBuilder.
                                setTitle(
                                        "Der Ping des Bots beträgt ⇒ " + (System.currentTimeMillis() - time) + " ms\n" +
                                        "Der Ping zum Discord Websocket beträgt ⇒ " + Core.getDiscordAPI().getJDA().getGatewayPing() + "ms")
                                .setColor(Color.GREEN).build()))
                .queue();
    }
}
