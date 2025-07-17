package de.cubeattack.proxymanager;

import java.util.List;

public interface ProxyInstance {

    int getOnlinePlayerCount();

    int getPlayerLimit();

    List<BackendServer> getBackendServerAsString();

    record BackendServer(String serverName, int playerCount) { }
}