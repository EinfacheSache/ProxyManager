package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ContextMenuListener extends ListenerAdapter {
    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (event.getGuild() != null && !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (event.getName().equals("Get user avatar")) {
            event.replyEmbeds(MessageUtils.getDefaultEmbed().setDescription("Avatar: " + event.getTarget().getEffectiveAvatarUrl()).build()).queue();
        }
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getGuild() != null && !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (event.getName().equals("Count words")) {
            event.replyEmbeds(MessageUtils.getDefaultEmbed().setDescription("Words: " + event.getTarget().getContentRaw().split("\\s+").length).build()).queue();
        }
    }
}
