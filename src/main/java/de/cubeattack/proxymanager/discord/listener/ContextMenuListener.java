package de.cubeattack.proxymanager.discord.listener;

import de.cubeattack.proxymanager.core.Core;
import de.cubeattack.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class ContextMenuListener extends ListenerAdapter {
    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (event.getName().equals("Get user avatar")) {
            event.replyEmbeds(MessageUtils.getDefaultEmbed().setDescription("Avatar: " + event.getTarget().getEffectiveAvatarUrl()).build()).queue();
        }
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (event.getName().equals("Count words")) {
            event.replyEmbeds(MessageUtils.getDefaultEmbed().setDescription("Words: " + event.getTarget().getContentRaw().split("\\s+").length).build()).queue();
        }
    }
}
