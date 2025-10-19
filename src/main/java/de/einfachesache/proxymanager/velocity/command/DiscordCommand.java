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

        final String url = Config.getDiscordLink();

        Component line1 = Component.empty()
                .append(Component.text("Für ", NamedTextColor.GRAY))
                .append(Component.text("Infos ", NamedTextColor.GOLD))
                .append(Component.text("und zukünftige ", NamedTextColor.GRAY))
                .append(Component.text("Flareon-Events", NamedTextColor.GOLD))
                .append(Component.newline());

        Component line2 = Component.empty()
                .append(Component.text("Tritt unserem ", NamedTextColor.GRAY))
                .append(Component.text("Discord", NamedTextColor.GOLD))
                .append(Component.text(" bei » ", NamedTextColor.GRAY));

        Component link = Component.text("Jetzt beitreten", NamedTextColor.AQUA, TextDecoration.BOLD)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(Component.empty()
                        .append(Component.text("Klicke, um unserem ", NamedTextColor.GRAY))
                        .append(Component.text("Discord ", NamedTextColor.GOLD))
                        .append(Component.text("beizutreten.\n", NamedTextColor.GRAY))
                        .append(Component.text(url, NamedTextColor.BLUE, TextDecoration.UNDERLINED))
                ));

        player.sendMessage(line1.append(line2).append(link));
    }
}
