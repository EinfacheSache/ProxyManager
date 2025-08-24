package de.einfachesache.proxymanager.velocity.proxyprotocol;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.network.ConnectionManager;
import com.velocitypowered.proxy.network.Connections;
import com.velocitypowered.proxy.network.ServerChannelInitializerHolder;
import com.velocitypowered.proxy.protocol.packet.KeepAlivePacket;
import de.einfachesache.api.util.rest.ResponseManager;
import de.einfachesache.api.util.rest.RestAPIUtils;
import de.einfachesache.proxymanager.core.Config;
import de.einfachesache.proxymanager.core.Core;
import de.einfachesache.proxymanager.velocity.VProxyManager;
import de.einfachesache.proxymanager.velocity.proxyprotocol.debugtool.DebugPingResponse;
import de.einfachesache.proxymanager.velocity.proxyprotocol.debugtool.KeepAliveResponseKey;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.epoll.EpollTcpInfo;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ProxyProtocol {

    private final Reflection.MethodInvoker initChannelMethod = Reflection.getMethod(ChannelInitializer.class, "initChannel", Channel.class);
    private final List<String> directConnectWhitelist = new ArrayList<>();
    private final List<Object> neoIPS = new ArrayList<>();

    private static final ConcurrentHashMap<String, ArrayList<DebugPingResponse>> debugPingResponses = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<KeepAliveResponseKey, Long> pingMap = new ConcurrentHashMap<>();

    public ProxyProtocol(VProxyManager instance) {

        neoIPS.addAll(getNeoIPs());

        Core.info("Proceeding with the server channel injection...");

        try {

            VelocityServer velocityServer = (VelocityServer) instance.getProxy();
            Reflection.FieldAccessor<ConnectionManager> connectionManagerFieldAccessor = Reflection.getField(VelocityServer.class, ConnectionManager.class, 0);
            ConnectionManager connectionManager = connectionManagerFieldAccessor.get(velocityServer);
            ChannelInitializer<?> oldInitializer = connectionManager.getServerChannelInitializer().get();

            ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel channel) {

                    try {

                        Core.debug("Open channel (" + channel.remoteAddress().toString() + ")");

                        initChannelMethod.getMethod().setAccessible(true);
                        initChannelMethod.invoke(oldInitializer, channel);

                        AtomicReference<InetSocketAddress> playerAddress = new AtomicReference<>();
                        String sourceAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();

                        if (channel.localAddress().toString().startsWith("local:") || sourceAddress.equals("Config.getGeyserServerIP()")) {
                            Core.debug("Detected bedrock player (return)");
                            return;
                        }

                        if (!directConnectWhitelist.contains(sourceAddress)) {
                            if (Config.isProxyProtocol() && (neoIPS.isEmpty() || neoIPS.stream().noneMatch(ipRange -> isIPInRange(String.valueOf(ipRange), sourceAddress)))) {
                                channel.close();
                                Core.debug("Close connection IP (" + channel.remoteAddress() + ") doesn't match to Neo-IPs (close / return)");
                                return;
                            }

                            Core.debug("Adding handler...");

                            if (Config.isProxyProtocol()) {
                                addProxyProtocolHandler(channel, playerAddress);
                                Core.debug("ProxyProtocol is on (Added proxyProtocolHandler)");
                            }

                            addKeepAlivePacketHandler(channel, playerAddress, velocityServer);
                            Core.debug("Added keepAlivePacketHandler");
                        }

                        Core.debug("Connecting finished");

                    } catch (Exception ex) {
                        Core.severe("Cannot inject incoming channel " + channel, ex);
                    }
                }
            };

            ServerChannelInitializerHolder newChannelHolder = (ServerChannelInitializerHolder) Reflection.getConstructor(ServerChannelInitializerHolder.class, ChannelInitializer.class).invoke(channelInitializer);
            Reflection.FieldAccessor<ServerChannelInitializerHolder> serverChannelInitializerHolderFieldAccessor = Reflection.getField(ConnectionManager.class, ServerChannelInitializerHolder.class, 0);
            Field channelInitializerHolderField = serverChannelInitializerHolderFieldAccessor.getField();

            channelInitializerHolderField.setAccessible(true);
            channelInitializerHolderField.set(connectionManager, newChannelHolder);

            Core.info("Found the server channel and added the handler. Injection successfully!");

        } catch (Exception ex) {
            Core.severe("An unknown error has occurred", ex);
        }
    }

    public void addProxyProtocolHandler(Channel channel, AtomicReference<InetSocketAddress> inetAddress) {
        channel.pipeline().names().forEach((n) -> {
            if (n.equals("HAProxyMessageDecoder#0"))
                channel.pipeline().remove("HAProxyMessageDecoder#0");
            if (n.equals("ProxyProtocol$1#0"))
                channel.pipeline().remove("ProxyProtocol$1#0");
        });

        channel.pipeline().addFirst("haproxy-decoder", new HAProxyMessageDecoder());
        channel.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof HAProxyMessage message) {
                    Reflection.FieldAccessor<SocketAddress> fieldAccessor = Reflection.getField(MinecraftConnection.class, SocketAddress.class, 0);
                    inetAddress.set(new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
                    fieldAccessor.set(channel.pipeline().get(Connections.HANDLER), inetAddress.get());
                } else {
                    super.channelRead(ctx, msg);
                }
            }
        });
    }

    public void addKeepAlivePacketHandler(Channel channel, AtomicReference<InetSocketAddress> inetAddress, VelocityServer velocityServer) {
        if (!channel.pipeline().names().contains("minecraft-decoder")) {
            Core.warn("Failed to add KeepAlivePacketHandler (minecraft-decoder can't be found)");
            return;
        }

        channel.pipeline().addAfter("minecraft-decoder", "proxy-keep-alive-handler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                super.channelRead(ctx, msg);

                if (!(msg instanceof KeepAlivePacket keepAlive)) {
                    return;
                }

                ConcurrentHashMap<KeepAliveResponseKey, Long> pingMap = ProxyProtocol.pingMap;

                Core.trace("Received KeepAlivePackets (" + keepAlive.getRandomId() + ")");

                for (KeepAliveResponseKey keepAliveResponseKey : pingMap.keySet()) {

                    if (!keepAliveResponseKey.getAddress().equals(inetAddress.get()) || !(keepAliveResponseKey.getId() == keepAlive.getRandomId())) {
                        continue;
                    }

                    Core.debug("KeepAlivePackets matched to DebugKeepAlivePacket");

                    for (Player player : velocityServer.getAllPlayers()) {

                        if (!(player).getRemoteAddress().equals(inetAddress.get())) {
                            continue;
                        }

                        Core.debug("Player matched to DebugKeepAlivePacket (loading data...)");

                        EpollTcpInfo tcpInfo = ((EpollSocketChannel) channel).tcpInfo();
                        EpollTcpInfo tcpInfoBackend = null;

                        if (((ConnectedPlayer) player).getConnectedServer() != null && ((ConnectedPlayer) player).getConnectedServer().getConnection() != null) {
                            tcpInfoBackend = ((EpollSocketChannel) ((ConnectedPlayer) player).getConnectedServer().getConnection().getChannel()).tcpInfo();
                        }

                        long ping = System.currentTimeMillis() - pingMap.get(keepAliveResponseKey);
                        long neoRTT = 0;
                        long backendRTT = 0;

                        if (tcpInfo != null) {
                            neoRTT = tcpInfo.rtt() / 1000;
                        }
                        if (tcpInfoBackend != null) {
                            backendRTT = tcpInfoBackend.rtt() / 1000;
                        }

                        ConcurrentHashMap<String, ArrayList<DebugPingResponse>> map = ProxyProtocol.debugPingResponses;

                        if (!map.containsKey(player.getUsername())) {
                            ProxyProtocol.debugPingResponses.put(player.getUsername(), new ArrayList<>());
                        }

                        map.get(player.getUsername()).add(new DebugPingResponse(ping, neoRTT, backendRTT, inetAddress.get(), channel.remoteAddress()));

                        Core.debug("Loading completed");
                        Core.debug(" ");

                    }
                    pingMap.remove(keepAliveResponseKey);
                }
            }
        });
    }

    public static boolean isIPInRange(String ipRange, String ipAddress) {
        if (!ipRange.contains("/")) {
            ipRange = ipRange + "/32";
        }

        long targetIntAddress = ipToDecimal(ipAddress);

        int range = Integer.parseInt(ipRange.split("/")[1]);
        String startIP = ipRange.split("/")[0];

        long startIntAddress = ipToDecimal(startIP);

        return targetIntAddress <= (startIntAddress + (long) Math.pow(2, (32 - range))) && targetIntAddress >= startIntAddress;
    }

    public static long ipToDecimal(String ipAddress) throws IllegalArgumentException {
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return -1;
        }

        long decimal = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(parts[i]);
            if (octet < 0 || octet > 255) {
                return -1;
            }
            decimal += (long) (octet * Math.pow(256, 3 - i));
        }

        return decimal;
    }

    private List<Object> getNeoIPs() {
        RestAPIUtils restAPIUtils = new RestAPIUtils();
        return new ResponseManager(restAPIUtils.request("GET", "https://api.neoprotect.net/v2/public/servers", null)).getResponseBodyArray().toList();
    }
}
