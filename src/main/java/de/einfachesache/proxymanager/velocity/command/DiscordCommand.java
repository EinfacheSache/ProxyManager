package de.einfachesache.proxymanager.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class DiscordCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {

        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendPlainMessage("You must be a player to use this command");
            return;
        }

        Component link = Component.text("Jetzt beitreten", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.copyToClipboard(Config.getDiscordLink()))
                .clickEvent(ClickEvent.openUrl(Config.getDiscordLink()))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Klicke, um unserem Discord beizutreten.\n", NamedTextColor.GRAY)
                                .append(Component.text(Config.getDiscordLink(), NamedTextColor.BLUE, TextDecoration.UNDERLINED))
                ));

        Component message = Component.text("Joinen unseren Discord: ", NamedTextColor.GOLD).append(link);

        player.sendMessage(message);
    }
}
