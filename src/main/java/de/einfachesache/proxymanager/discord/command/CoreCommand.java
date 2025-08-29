package de.einfachesache.proxymanager.discord.command;

import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.DiscordAPI;
import de.einfachesache.proxymanager.discord.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class CoreCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getGuild() != null && !Config.getGuildIDs().contains(event.getGuild().getId())) return;
        if (!event.getName().equalsIgnoreCase("core")) return;

        if (!Objects.equals(event.getUser().getIdLong(), DiscordAPI.DEV_USER_ID)) {
            event.replyEmbeds(
                    MessageUtils.getErrorEmbed()
                            .setDescription("You are not allowed to use this command!")
                            .build())
                    .setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).setEphemeral(true).queue();
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed();

        switch (Objects.requireNonNull(event.getSubcommandName())) {

            case "register" -> embedBuilder.setDescription("Coming soon!");

            case "restart" -> {
                embedBuilder.setDescription("Restart wird ausgeführt!");
                Core.shutdown();
                Core.run();
            }

            case "invite-link" -> {

                if (!(event.getChannel() instanceof TextChannel channel)) {
                    embedBuilder.setDescription("Command kann hier nicht ausgeführt werden!");
                    break;
                }

                channel.createInvite().setMaxAge(0).queue(invite -> {
                    embedBuilder.setDescription(invite.getUrl());
                    event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                });

                return;
            }

            case "reload-commands" -> {

                if (event.getGuild() == null) {
                    embedBuilder.setDescription("Command kann nur auf einem Server ausgeführt werden!");
                    break;
                }

                Guild guild = event.getGuild();
                Core.getDiscordAPI().loadGuildDiscordCommands(guild);
                embedBuilder.setDescription("Commands wurden reloaded!");
                Core.info(guild.getName() + " | Updating Discord slash commands");
            }

            case "reload-commands-global" -> {
                Core.getDiscordAPI().loadGlobalDiscordCommands();
                event.getJDA().getGuilds().forEach(guild -> Core.getDiscordAPI().loadGuildDiscordCommands(guild));
                embedBuilder.setDescription("Commands wurden global reloaded!");
                Core.info((event.getGuild() != null ? event.getGuild().getName() + " | " : "") + "Updating Discord slash commands global");
            }
        }
        event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
