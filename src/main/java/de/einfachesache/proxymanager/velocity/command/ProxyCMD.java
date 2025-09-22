package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.core.RedisConnector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProxyCMD implements SimpleCommand {

    private final List<String> subCommands = Arrays.asList("reload", "commands", "chat");
    private final List<String> reloadSubs = List.of("config", "discord");
    private final List<String> enableAndDisableSubs = List.of("enable", "disable");

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length >= 1) {

            switch (args[0]) {
                case "reload", "rl" :{
                    reloadCMD(source, args);
                    return;
                }

                case "commands" :{
                    commandsCMD(source, args);
                    return;
                }

                case "chat" :{
                    chatCMD(source, args);
                    return;
                }
            }
        }

        source.sendMessage(Component.text("§cInvalid arguments -> /proxy [args]"));
    }

    private void reloadCMD(CommandSource source, String[] args) {

        if (args.length == 1) {
            source.sendMessage(Component.text("Reload: Config + Discord …").color(NamedTextColor.GRAY));
            Config.reloadFilesAsync().thenAccept(ok -> {
                if (ok) source.sendMessage(Component.text("§aConfigs neu geladen."));
                else source.sendMessage(Component.text("§cConfig Reload fehlgeschlagen. Details im Log."));
            });
            Core.getDiscordAPI().reloadGuildsAsync().thenAccept(ok -> {
                if (ok) source.sendMessage(Component.text("§aDiscord-Commands neu geladen."));
                else source.sendMessage(Component.text("§cDiscord Reload fehlgeschlagen. Details im Log."));
            });
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {

            case "config" -> {
                source.sendMessage(Component.text("Reload: Config …").color(NamedTextColor.GRAY));
                Config.reloadFilesAsync().thenAccept(ok -> {
                    if (ok) source.sendMessage(Component.text("§aConfigs neu geladen."));
                    else source.sendMessage(Component.text("§cConfig Reload fehlgeschlagen. Details im Log."));
                });
                source.sendMessage(Component.text("Config neu geladen.").color(NamedTextColor.GREEN));
            }

            case "discord" -> {
                source.sendMessage(Component.text("Reload: Discord …").color(NamedTextColor.GRAY));
                Core.getDiscordAPI().reloadGuildsAsync().thenAccept(ok -> {
                    if (ok) source.sendMessage(Component.text("§aDiscord-Commands neu geladen."));
                    else source.sendMessage(Component.text("§cDiscord Reload fehlgeschlagen. Details im Log."));
                });
            }

            default -> {
                source.sendMessage(Component.text("Unbekanntes Argument: " + args[1].toLowerCase(Locale.ROOT)).color(NamedTextColor.RED));
                source.sendMessage(Component.text("Nutzung: /proxy reload [config|discord]").color(NamedTextColor.RED));
            }
        }
    }

    private void commandsCMD(CommandSource source, String[] args) {
        RedisConnector jedis = Core.getRedisConnector();

        if (args.length == 1) {
            boolean disabled = Boolean.parseBoolean(jedis.get("Commands-Disabled"));
            if (disabled) {
                source.sendMessage(Component.text("§aDu hast alle Commands aktiviert"));
                jedis.set("Commands-Disabled", "false");
            } else {
                source.sendMessage(Component.text("§4Du hast alle Commands deaktiviert"));
                jedis.set("Commands-Disabled", "true");
            }
            return;
        }


        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("enable")) {
                source.sendMessage(Component.text("§aDu hast alle Commands aktiviert"));
                jedis.set("Commands-Disabled", "false");
            } else if (args[1].equalsIgnoreCase("disable")) {
                source.sendMessage(Component.text("§4Du hast alle Commands deaktiviert"));
                jedis.set("Commands-Disabled", "true");
            }
        }
    }

    private void chatCMD(CommandSource source, String[] args) {
        RedisConnector jedis = Core.getRedisConnector();

        if (args.length == 1) {
            boolean chatDisabled = Boolean.parseBoolean(jedis.get("Chat-Disabled"));
            if (chatDisabled) {
                source.sendMessage(Component.text("§aDu hast den Chat aktiviert"));
                jedis.set("Chat-Disabled", "false");
            } else {
                source.sendMessage(Component.text("§4Du hast den Chat deaktiviert"));
                jedis.set("Chat-Disabled", "true");
            }
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("enable")) {
                source.sendMessage(Component.text("§aDu hast den Chat aktiviert"));
                jedis.set("Chat-Disabled", "false");
            } else if (args[1].equalsIgnoreCase("disable")) {
                source.sendMessage(Component.text("§4Du hast den Chat deaktiviert"));
                jedis.set("Chat-Disabled", "true");
            }
        }
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return subCommands;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return subCommands.stream()
                    .filter(sub -> sub.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        String first = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);

            if (first.equals("reload") || first.equals("rl")) {
                return reloadSubs.stream()
                        .filter(sub -> sub.startsWith(prefix))
                        .collect(Collectors.toList());
            }

            if (first.equals("commands") || first.equals("chat")) {
                return enableAndDisableSubs.stream()
                        .filter(sub -> sub.startsWith(prefix))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("*");
    }
}