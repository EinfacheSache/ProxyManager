package de.cubeattack.proxymanager.velocity.unsign.command;

import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.CommandHandler;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket;
import de.cubeattack.proxymanager.velocity.VelocityProxyManager;
import de.cubeattack.proxymanager.velocity.unsign.message.PacketReceiveEvent;

import java.util.concurrent.CompletableFuture;

public final class SessionCommandListener implements CommandHandler<SessionPlayerCommandPacket> {

    private final VelocityServer proxyServer;

    @Inject
    public SessionCommandListener(VelocityProxyManager plugin) {
        this.proxyServer = (VelocityServer) plugin.getProxy();
        this.proxyServer.getEventManager().register(plugin, PacketReceiveEvent.class, this::onCommand);
    }


    public void onCommand(final PacketReceiveEvent event) {
        if (!(event.getPacket() instanceof SessionPlayerCommandPacket packet)) {
            return;
        }

        final ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        if (checkConnectionFailed(player)) return;

        event.setResult(ResultedEvent.GenericResult.denied());
        final String commandExecuted = packet.getCommand();

        queueCommandResult(
                proxyServer,
                player,
                (commandEvent, chatState) -> {
                    final CommandExecuteEvent.CommandResult result = commandEvent.getResult();
                    if (result == CommandExecuteEvent.CommandResult.denied()) {
                        return CompletableFuture.completedFuture(null);
                    }

                    final String commandToRun = result.getCommand().orElse(commandExecuted);
                    if (result.isForwardToServer()) {
                        if (commandToRun.equals(commandExecuted)) {
                            return CompletableFuture.completedFuture(packet);
                        } else {
                            return CompletableFuture.completedFuture(player.getChatBuilderFactory()
                                    .builder()
                                    .setTimestamp(packet.getTimeStamp())
                                    .asPlayer(player)
                                    .message("/" + commandToRun)
                                    .toServer());
                        }
                    }

                    return runCommand(proxyServer, player, commandToRun, hasRun -> {
                        if (hasRun) return null;

                        if (commandToRun.equals(commandExecuted)) {
                            return packet;
                        } else {
                            return player.getChatBuilderFactory()
                                    .builder()
                                    .setTimestamp(packet.getTimeStamp())
                                    .asPlayer(player)
                                    .message("/" + commandToRun)
                                    .toServer();
                        }
                    });
                },
                commandExecuted,
                packet.getTimeStamp(),
                new LastSeenMessages(),
                new CommandExecuteEvent.InvocationInfo(packet.getEventSignedState(), null)
        );
    }

    @Override
    public Class<SessionPlayerCommandPacket> packetClass() {
        return SessionPlayerCommandPacket.class;
    }

    @Override
    public void handlePlayerCommandInternal(SessionPlayerCommandPacket sessionPlayerCommand) {
        // noop
    }

    public boolean checkConnectionFailed(final ConnectedPlayer player) {
        try {
            player.ensureAndGetCurrentServer().ensureConnected();
            return false;
        } catch (final IllegalStateException e) {
            return true;
        }
    }
}
