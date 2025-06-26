package de.cubeattack.proxymanager.discord.listener;

import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class MemberGuildJoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;

        Role role = event.getGuild().getRoleById(Config.getUserRoleID());

        if (role == null) {
            Core.info("❌ Rolle nicht gefunden: " + Config.getUserRoleID());
            return;
        }

        event.getGuild().addRoleToMember(event.getMember(), role).queue(
                success -> Core.info("✅ Rolle wurde dem neuen Mitglied zugewiesen."),
                error -> Core.info("⚠️ Fehler beim Rollenzuweisen: " + error.getMessage())
        );
    }
}
