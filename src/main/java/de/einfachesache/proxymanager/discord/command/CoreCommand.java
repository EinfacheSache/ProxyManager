package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class CoreCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.isFromGuild() & !Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;
        if (!event.getName().equalsIgnoreCase("core")) return;

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();

        if(!Objects.equals(event.getUser().getIdLong(), DiscordAPI.DEV_USER_ID)){
            embedBuilder.setDescription("You are not allowed to use this command!");
            embedBuilder.setColor(Color.RED);
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).setEphemeral(true).queue();

        switch (Objects.requireNonNull(event.getSubcommandName())) {

            case "register" -> embedBuilder.setDescription("Coming soon!");

            case "restart" -> {
                embedBuilder.setDescription("Restart wird ausgeführt!");
                Core.shutdown();
                Core.run();
            }

            case "reload-commands" -> {

                if(event.getGuild() == null){
                    embedBuilder.setDescription("Command kann nur auf einem Server ausgeführt werden!");
                    break;
                }

                Core.getDiscordAPI().loadGuildDiscordCommands(event.getGuild());
                embedBuilder.setDescription("Commands wurden reloaded!");
                Core.info("Updating Discord slash commands");
            }

            case "reload-commands-global" -> {
                Core.getDiscordAPI().loadGlobalDiscordCommands();
                event.getJDA().getGuilds().forEach(guild -> Core.getDiscordAPI().loadGuildDiscordCommands(guild));
                embedBuilder.setDescription("Commands wurden global reloaded!");
                Core.info("Updating Discord slash commands global");
            }
        }
        event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
