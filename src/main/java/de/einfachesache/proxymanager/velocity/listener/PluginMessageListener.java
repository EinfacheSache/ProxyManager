package de.einfachesache.proxymanager.velocity.listener;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.ServerConnection;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.discord.listener.TicketListener;
import de.einfachesache.proxymanager.velocity.VProxyManager;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PluginMessageListener {

    private final VProxyManager instance;
    private final Gson gson = new Gson();

    public PluginMessageListener(VProxyManager instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(VProxyManager.TICKET)) return;

        if (!(event.getSource() instanceof ServerConnection serverConnection)) {
            Core.warn("Dropping ticket message not from a ServerConnection");
            return;
        }

        var in = ByteStreams.newDataInput(event.getData());
        String json = in.readUTF();

        TicketPayload payload = gson.fromJson(json, TicketPayload.class);
        payload.context.server = serverConnection.getServerInfo().getName();

        instance.getProxy().getPlayer(UUID.fromString(payload.reporter.uuid)).ifPresent(player -> {
            ProtocolVersion pVersion = player.getProtocolVersion();
            payload.context.protocol = String.valueOf(pVersion.getProtocol());
            payload.context.version = pVersion.getMostRecentSupportedVersion();
        });

        TicketListener.createBugReportTicket(
                Config.getAssignedGuildID(),
                Config.getWhitelistedPlayers().entrySet().stream()
                        .filter(e -> Objects.equals(e.getValue().toLowerCase(), payload.reporter.name.toLowerCase()))
                        .map(Map.Entry::getKey).toList().getFirst(),
                buildTicketText(payload));
    }

    public static String buildTicketText(PluginMessageListener.TicketPayload p) {
        if (p == null) return "Bug-Report: (kein Payload)";

        var r = (p.reporter != null) ? p.reporter : new PluginMessageListener.TicketPayload.Reporter();
        var c = (p.context != null) ? p.context : new PluginMessageListener.TicketPayload.Context();

        String msg = cleanMsg(p.message);
        String held = (c.heldItem == null || "AIR".equalsIgnoreCase(c.heldItem)) ? "-" : c.heldItem;
        String pos = (Double.isFinite(c.x) && Double.isFinite(c.y) && Double.isFinite(c.z))
                ? String.format("(%.1f / %.1f / %.1f)", c.x, c.y, c.z)
                : "-";

        return "```yml\n" +
                "[BUG-REPORT]\n" +
                "Spieler: " + nz(r.name) + "(" + nz(r.uuid).replace("-", "") + ")\n" +
                "Kategorie: " + nz(p.category) + "\n" +
                "Position: " + capitalizeFirst(nz(c.world)) + " / " + pos + "\n" +
                "Gamemode: " + nz(c.gamemode) + "\n" +
                "Phase: " + nz(c.phase) + "\n" +
                "Client: " + capitalizeFirst(nz(c.client)) + " · Ping: " + c.ping + "ms\n" +
                "Version: "  + nz(c.version) + " · Protocol: " + nz(c.protocol) + "\n" +
                "Performance: TPS " + c.tps + " · MSPT " + c.mspt + "ms/tick\n" +
                "Item: " + held + "\n" +
                "Team: " + nz(c.team) + "\n" +
                "Nachricht: " + indentMultiline(capitalizeFirst(msg)) + "```";
    }

    static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    private static String cleanMsg(String s) {
        if (s == null) return "-";
        s = s.replace("\r", "").trim();
        if (s.isEmpty()) return "-";
        return s;
    }

    private static String indentMultiline(String s) {
        if ("-".equals(s)) return s;
        String[] lines = s.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append("  ").append(line).append("\n");
        return sb.toString().trim();
    }

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    public static final class TicketPayload {
        public String category;
        public String message;
        public Reporter reporter;
        public Context context;

        public static final class Reporter {
            public String uuid;
            public String name;
        }

        public static final class Context {
            public String server;
            public int ping;
            public double x, y, z;
            public double tps, mspt;
            public String world, client, version, protocol;
            public String gamemode, heldItem, phase, team;
        }
    }
}
