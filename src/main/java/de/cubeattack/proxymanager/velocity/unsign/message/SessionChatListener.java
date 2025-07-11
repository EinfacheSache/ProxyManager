package de.cubeattack.proxymanager.velocity.unsign.message;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket;
import de.cubeattack.proxymanager.velocity.VelocityProxyManager;
public final class SessionChatListener {

    private final VelocityProxyManager plugin;

    public SessionChatListener(VelocityProxyManager plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onChat(PacketReceiveEvent event) {

        if (!(event.getPacket() instanceof SessionPlayerChatPacket chatPacket)) {
            return;
        }

        ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        String chatMessage = chatPacket.getMessage();

        if (!checkConnection(player)) return;

        event.setResult(ResultedEvent.GenericResult.denied());

        player.getChatQueue().queuePacket(
                lastSeenMessages -> plugin.getProxy().getEventManager()
                        .fire(new PlayerChatEvent(player, chatMessage))
                        .thenApply(PlayerChatEvent::getResult)
                        .thenApply(result -> {
                            if (!result.isAllowed()) {
                                return player.getChatBuilderFactory()
                                        .builder()
                                        .setTimestamp(chatPacket.getTimestamp())
                                        .message("").toClient(); // kein Text = keine Anzeige

                            }

                            final boolean isModified = result
                                    .getMessage()
                                    .map(msg -> !msg.equals(chatMessage))
                                    .orElse(false);

                            if (isModified) {
                                return player.getChatBuilderFactory()
                                        .builder()
                                        .message(result.getMessage().get())
                                        .setTimestamp(chatPacket.getTimestamp())
                                        .toServer();
                            }

                            return chatPacket;
                        }),
                chatPacket.getTimestamp(),
                chatPacket.getLastSeenMessages()
        );
    }

    public boolean checkConnection(final ConnectedPlayer player) {
        try {
            player.ensureAndGetCurrentServer().ensureConnected();
            return true;
        } catch (final IllegalStateException e) {
            return false;
        }
    }
}