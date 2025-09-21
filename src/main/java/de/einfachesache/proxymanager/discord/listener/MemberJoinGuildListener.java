package de.einfachesache.proxymanager.discord.listener;

import de.einfachesache.api.util.LogUtils;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
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
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MemberJoinGuildListener extends ListenerAdapter {

    private final static Map<String, Integer> INVITE_USES = new HashMap<>();
    private final int maxBetaTesterCount = 100;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!Config.getGuildIDs().contains(event.getGuild().getId())) return;

        sendWelcomeImage(event);
        trackInvite(event);

        Guild guild = event.getGuild();
        Role playerRole = guild.getRoleById(Config.getJoinRoleID(guild.getId()));
        Role betaTesterRole = guild.getRoleById(Config.getBetaTesterRoleID(guild.getId()));

        if (playerRole == null) {
            Core.info(guild.getName() + " | ❌Join Rollen wurden für (" + event.getUser().getName() + ") nicht gefunden: " + Config.getJoinRoleID(guild.getId()));
            return;
        }

        int currentBetaTesterCount = (int) guild.getMembers().stream()
                .filter(m -> m.getRoles().contains(betaTesterRole))
                .count();

        List<Role> rolesToAdd = new ArrayList<>(Collections.singletonList(playerRole));
        if (!Config.getBetaTesterRoleID(guild.getId()).equals("-1") && currentBetaTesterCount + 1 <= maxBetaTesterCount) {
            rolesToAdd.add(betaTesterRole);
        } else {
            Config.setBetaTesterRoleID(guild.getId(), "-1");
        }

        guild
                .modifyMemberRoles(
                        event.getMember(),
                        rolesToAdd,
                        Collections.emptyList())
                .reason("Automatische Zuweisung nach Beitritt")
                .queue(success -> {
                            String rollen = rolesToAdd.stream()
                                    .map(Role::getName)
                                    .collect(Collectors.joining(", "));

                            String userName = event.getMember().getUser().getName();

                            String info = guild.getName() + " | ✅ Zugewiesene Rollen: " + rollen + " | User: " + userName;

                            if (rolesToAdd.contains(betaTesterRole)) {
                                info += "(BetaTester #" + (currentBetaTesterCount + 1) + "/" + maxBetaTesterCount + ")";
                            }

                            Core.info(info);
                        },
                        error -> Core.info(guild.getName() + " | ⚠️ Fehler beim Rollenzuweisen | User: " + event.getMember().getUser().getName() + " Error: " + error.getMessage())
                );
    }

    public void trackInvite(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        TextChannel inviteLogChannel = guild.getChannelById(TextChannel.class, Config.getInviteLogChannelID(guild.getId()));
        Path logFilePath = Path.of((Core.isMinecraftServer() ? "plugins/ProxyManager/" : "./") + "logs/Invites.log");

        guild.retrieveInvites().queue(invites -> {

            boolean foundInviter = false;

            for (Invite invite : invites) {
                int oldUses = INVITE_USES.getOrDefault(invite.getCode(), 0);
                if (invite.getUses() > oldUses) {
                    INVITE_USES.put(invite.getCode(), invite.getUses());
                    User inviter = invite.getInviter();

                    if (inviter == null) {
                        Core.severe(guild.getName() + " | Member joined, but inviter is null. Possibly the inviter has left the server. User: " + event.getUser().getName());
                        return;
                    }

                    foundInviter = true;

                    if (inviteLogChannel != null)
                        inviteLogChannel.sendMessage("📥 " + event.getMember().getAsMention() + " wurde eingeladen von " + inviter.getAsMention()).queue();
                    LogUtils.write(logFilePath, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " " + event.getUser().getName() + " wurde eingeladen von " + inviter.getName());

                    Config.addEligibleUserForGiveaway(guild.getId(), inviter.getId());

                    break;
                }
            }

            if (!foundInviter && guild.getVanityUrl() != null) {
                guild.retrieveVanityInvite().queue(vanity -> {
                    String code = vanity.getCode();
                    int uses = vanity.getUses();


                    if (inviteLogChannel != null) {
                        inviteLogChannel.sendMessage("📥 " + event.getMember().getAsMention() +
                                " ist dem Server über die Vanity-URL `discord.gg/" + code + "` beigetreten. " +
                                "(**Total: " + uses + "**)").queue();
                    }

                    LogUtils.write(logFilePath,
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " " +
                                    event.getUser().getName() + " ist über Vanity-URL discord.gg/" + code + " beigetreten. (Total: " + uses + ")");
                }, error -> Core.severe(guild.getName() + " | Error while retrieving the vanity URL: " + error.getMessage()));
            }
        });
    }


    public void sendWelcomeImage(GuildMemberJoinEvent event) {
        try {
            Member member = event.getMember();
            Guild guild = event.getGuild();
            TextChannel channel = Objects.requireNonNull(guild.getSystemChannel());

            // Hintergrundbild laden
            BufferedImage background = ImageIO.read(Objects.requireNonNull(getClass().getResource("/flareon_dragon.png")));
            int width = background.getWidth();
            int height = background.getHeight();

            Graphics2D g = background.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Skalierung
            double scaleFactor = width / 1600.0;
            int avatarSize = (int) (450 * scaleFactor);
            int baseFontSize = (int) (100 * scaleFactor);
            int verticalGap = (int) (75 * scaleFactor);

            // Avatar laden
            BufferedImage avatar = ImageIO.read(URI.create(member.getEffectiveAvatarUrl() + "?size=512").toURL());
            int avatarX = (width - avatarSize) / 2;

            // Willkommen-Text
            String line1 = "Willkommen " + member.getEffectiveName() + " bei";
            String line2 = guild.getName().toUpperCase() + "!";

            // Dynamische Schriftgröße
            int fontSize = baseFontSize;
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int maxTextWidth = (int) (width * 0.9);

            while ((fm.stringWidth(line1) > maxTextWidth || fm.stringWidth(line2) > maxTextWidth)
                    && fontSize > 20) {
                fontSize--;
                font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
                g.setFont(font);
                fm = g.getFontMetrics();
            }
            int lineHeight = fm.getHeight();

            // Gesamtblock vertikal ausrichten
            int totalTextHeight = lineHeight * 2;
            int totalBlockHeight = avatarSize + verticalGap + totalTextHeight;
            int avatarY = (height - totalBlockHeight) / 2;

            // Avatar rund zeichnen
            g.setClip(new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize));
            g.drawImage(avatar, avatarX, avatarY, avatarSize, avatarSize, null);
            g.setClip(null);

            // Textposition
            int textY1 = avatarY + avatarSize + verticalGap + fm.getAscent();
            int textY2 = textY1 + lineHeight;
            int textX1 = (width - fm.stringWidth(line1)) / 2;
            int textX2 = (width - fm.stringWidth(line2)) / 2;

            // --- Blur + Liquid Glass Hintergrund für Text ---
            int paddingX = (int) (40 * scaleFactor);
            int paddingY = (int) (20 * scaleFactor);

            int blockX = Math.max(0, Math.min(textX1, textX2) - paddingX);
            int blockY = Math.max(0, textY1 - fm.getAscent() - paddingY);
            int blockWidth = Math.min(width - blockX, Math.max(fm.stringWidth(line1), fm.stringWidth(line2)) + 2 * paddingX);
            int blockHeight = Math.min(height - blockY, (textY2 - textY1) + lineHeight + 2 * paddingY);

            // Bereich für Blur ausschneiden
            BufferedImage blurSrc = background.getSubimage(blockX, blockY, blockWidth, blockHeight);

            // Blur-Filter (12x12, weicher Blur)
            float[] kernelData = new float[15 * 15];
            Arrays.fill(kernelData, 1.0f / kernelData.length);
            Kernel kernel = new Kernel(12, 12, kernelData);
            ConvolveOp blur = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            BufferedImage blurred = blur.filter(blurSrc, null);

            // Geblurrten Bereich zurückzeichnen
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(blurred, blockX, blockY, null);

            // Halbtransparentes, abgerundetes Overlay ("Liquid Glass")
            Color glassGray = new Color(50, 50, 50, 110); // noch etwas durchsichtiger
            g.setColor(glassGray);
            g.fillRoundRect(blockX, blockY, blockWidth, blockHeight, (int) (60 * scaleFactor), (int) (60 * scaleFactor));

            // Optional: weißer Glow-Rand
            g.setColor(new Color(255, 255, 255, 60));
            g.setStroke(new BasicStroke((float) (6 * scaleFactor)));
            g.drawRoundRect(blockX, blockY, blockWidth, blockHeight, (int) (60 * scaleFactor), (int) (60 * scaleFactor));

            g.setComposite(AlphaComposite.SrcOver);

            // --- Farbverlauf-Text wie gehabt ---
            GradientPaint textGradient1 = new GradientPaint(
                    textX1, textY1 - fm.getAscent(), new Color(183, 52, 234),
                    textX1 + fm.stringWidth(line1), textY1 - fm.getAscent(), new Color(93, 93, 246)
            );
            g.setFont(font);
            g.setPaint(textGradient1);
            g.drawString(line1, textX1, textY1);

            GradientPaint textGradient2 = new GradientPaint(
                    textX2, textY2 - fm.getAscent(), new Color(183, 52, 234),
                    textX2 + fm.stringWidth(line2), textY2 - fm.getAscent(), new Color(93, 93, 246)
            );
            g.setPaint(textGradient2);
            g.drawString(line2, textX2, textY2);

            g.dispose();

            // Bild in PNG-Bytearray umwandeln
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(background, "png", baos);
            byte[] imageData = baos.toByteArray();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎉 Willkommen bei " + guild.getName())
                    .setDescription("Hi " + member.getAsMention() + ", schön dass du da bist!")
                    .setImage("attachment://welcome.png")
                    .setColor(new Color(93, 93, 246)); // Passend zum Verlauf

            channel.sendFiles(FileUpload.fromData(imageData, "welcome.png"))
                    .setEmbeds(embed.build())
                    .queue();

        } catch (Exception e) {
            Core.severe("Error in sendWelcomeImage: " + e.getMessage(), e);
        }

    }

    public static Map<String, Integer> getInviteUses() {
        return INVITE_USES;
    }
}