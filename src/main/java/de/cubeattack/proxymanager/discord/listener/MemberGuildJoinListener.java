package de.cubeattack.proxymanager.discord.listener;

import de.cubeattack.api.util.Logs;
import de.cubeattack.proxymanager.core.Config;
import de.cubeattack.proxymanager.core.Core;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MemberGuildJoinListener extends ListenerAdapter {

    private static final Map<String, Integer> inviteUses = new HashMap<>();

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!Objects.equals(event.getGuild(), Core.getDiscordAPI().getGuild())) return;

        trackInvite(event);
        sendWelcomeImage(event);

        Role playerRole = event.getGuild().getRoleById(Config.getUserRoleID());
        Role betaTesterRole = event.getGuild().getRoleById(Config.getBetaTesterRoleID());

        if (playerRole == null || betaTesterRole == null) {
            Core.info("❌Join Rollen wurden nicht gefunden: " + Config.getUserRoleID() + " | " + Config.getBetaTesterRoleID());
            return;
        }

        event.getGuild()
                .modifyMemberRoles(
                        event.getMember(),
                        List.of(playerRole, betaTesterRole),
                        Collections.emptyList())
                .reason("Automatische Zuweisung nach Beitritt") // optional
                .queue(
                        success -> Core.info("✅ Rollen Spieler & BetaTester zugewiesen."),
                        error   -> Core.info("⚠️ Fehler beim Rollenzuweisen: " + error.getMessage())
                );
    }

    public void trackInvite(GuildMemberJoinEvent event) {
        event.getGuild().retrieveInvites().queue(invites -> {
            for (Invite invite : invites) {
                int oldUses = inviteUses.getOrDefault(invite.getCode(), 0);
                if (invite.getUses() > oldUses) {
                    inviteUses.put(invite.getCode(), invite.getUses());
                    User inviter = invite.getInviter();

                    if(inviter == null) {
                        Core.severe("Member joined, but inviter is null. Possibly joined via deleted invite or vanity URL. User: " + event.getUser().getAsTag());
                        return;
                    }

                    Objects.requireNonNull(event.getGuild().getChannelById(TextChannel.class, "1389721957318000825"))
                            .sendMessage("📥 " + event.getMember().getAsMention() + " wurde eingeladen von " + inviter.getAsMention())
                            .queue();
                    Logs.write(Path.of("logs/Invites.log"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " " + event.getUser().getName() + " wurde eingeladen von " + inviter.getName());
                    break;
                }
            }
        });
    }


    public void sendWelcomeImage(GuildMemberJoinEvent event) {
        try {
            Member member = event.getMember();
            Guild guild = event.getGuild();
            TextChannel channel = Objects.requireNonNull(guild.getDefaultChannel()).asTextChannel();

            // Hintergrund laden
            BufferedImage background = ImageIO.read(Objects.requireNonNull(getClass().getResource("/background.png")));
            int width = background.getWidth();
            int height = background.getHeight();

            Graphics2D g = background.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Skalierungsfaktor (bezogen auf Breite 1600)
            double scaleFactor = width / 1600.0;

            // Basisgrößen
            int avatarSize = (int) (450 * scaleFactor);
            int baseFontSize = (int) (100 * scaleFactor);
            int verticalGap = (int) (75 * scaleFactor);

            // Avatar laden
            BufferedImage avatar = ImageIO.read(new URL(member.getEffectiveAvatarUrl() + "?size=512"));
            int avatarX = (width - avatarSize) / 2;

            // Zeilen
            String line1 = "Willkommen " + member.getEffectiveName() + " bei";
            String line2 = guild.getName().toUpperCase() + "!";

            // Dynamische Schriftgröße finden
            int fontSize = baseFontSize;
            Font font = new Font("SansSerif", Font.BOLD, fontSize);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int maxTextWidth = (int) (width * 0.9);  // max. 90% der Breite

            while ((fm.stringWidth(line1) > maxTextWidth || fm.stringWidth(line2) > maxTextWidth)
                    && fontSize > 20) {
                fontSize--;
                font = new Font("SansSerif", Font.BOLD, fontSize);
                g.setFont(font);
                fm = g.getFontMetrics();
            }
            int lineHeight = fm.getHeight();

            // Gesamt-Block mittig vertikal ausrichten
            int totalTextHeight = lineHeight * 2;
            int totalBlockHeight = avatarSize + verticalGap + totalTextHeight;

            // Avatar zeichnen
            int avatarY = (height - totalBlockHeight) / 2;
            g.setClip(new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize));
            g.drawImage(avatar, avatarX, avatarY, avatarSize, avatarSize, null);
            g.setClip(null);

            // Text mittig zeichnen
            g.setFont(font);
            g.setColor(Color.WHITE);

            int textY1 = avatarY + avatarSize + verticalGap + fm.getAscent();
            int textY2 = textY1 + lineHeight;
            int textX1 = (width - fm.stringWidth(line1)) / 2;
            int textX2 = (width - fm.stringWidth(line2)) / 2;

            g.drawString(line1, textX1, textY1);
            g.drawString(line2, textX2, textY2);

            g.dispose();

            // Als PNG an Discord schicken
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(background, "png", baos);
            byte[] imageData = baos.toByteArray();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎉 Willkommen bei " + guild.getName())
                    .setDescription("Hi " + member.getAsMention() + ", schön dass du da bist!")
                    .setImage("attachment://welcome.png")
                    .setColor(Color.CYAN);

            channel.sendFiles(FileUpload.fromData(imageData, "welcome.png"))
                    .setEmbeds(embed.build())
                    .queue();

        } catch (Exception e) {
            Core.severe("Error: " + e.getMessage(), e);
        }
    }

    public static Map<String, Integer> getInviteUses() {
        return inviteUses;
    }
}