package de.einfachesache.proxymanager.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.velocity.ScreenBuilder;

public class LoginHostFilterListener {

    @Subscribe()
    public void onLogin(LoginEvent event) {
        Player connection = event.getPlayer();
        String virtualHost = getVirtualHost(connection);

        if (ipInCidrs(virtualHost) || hostAllowed(virtualHost)) {
            return;
        }

        connection.disconnect(new ScreenBuilder()
                .addLine("§c§l✖ Verbindung nicht möglich ✖")
                .addEmptyLine()
                .addLine("§7Login über §e" + virtualHost + " §7ist nicht erlaubt.")
                .addLine("§7Bitte verbinde dich über: §b" + Config.getServerDomainName())
                .build());
    }

    public static boolean hostAllowed(String host) {
        if (host == null || host.isEmpty()) return false;
        String h = host.toLowerCase();
        for (String p : Config.getAllowedDomains()) {
            String pat = p.toLowerCase();
            if (pat.startsWith("*.")) {
                String suffix = pat.substring(1); // ".domain.de"
                if (h.endsWith(suffix) && h.length() > suffix.length()) return true; // mind. ein Label davor
            } else if (h.equals(pat)) return true;
        }
        return false;
    }

    public static boolean ipInCidrs(String ip) {
        try {
            var addr = java.net.InetAddress.getByName(ip);
            if (!(addr instanceof java.net.Inet4Address)) return false;
            int ipInt = bytesToInt(addr.getAddress());
            for (String cidr : Config.getAllowedSubnet()) {
                String[] parts = cidr.split("/");
                int maskBits = Integer.parseInt(parts[1]);
                int mask = maskBits == 0 ? 0 : -(1 << (32 - maskBits));
                int net = bytesToInt(java.net.InetAddress.getByName(parts[0]).getAddress()) & mask;
                if ((ipInt & mask) == net) return true;
            }
            return false;
        } catch (Exception e) { return false; }
    }

    private static int bytesToInt(byte[] b) {
        return ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
    }

    private String getVirtualHost(InboundConnection connection) {
        if (connection.getVirtualHost().isEmpty()) {
            return "not_found";
        }
        return connection.getVirtualHost().get().getHostName();
    }
}
