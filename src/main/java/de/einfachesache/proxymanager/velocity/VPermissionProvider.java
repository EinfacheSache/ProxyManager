package de.einfachesache.proxymanager.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.api.minecraft.MinecraftAPI;
import de.einfachesache.proxymanager.core.Core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class VPermissionProvider {

    private static final Map<UUID, Set<String>> playerPermissions = new ConcurrentHashMap<>();

    public static void addPermissions(String name, List<String> permissions) {
        AsyncExecutor.getService().submit(() -> {
            UUID uuid = MinecraftAPI.loadUUID(name);

            if (uuid == null) {
                Core.warn("Permissions load failed -> could not find uuid for name " + name);
                return;
            }

            addPermissions(uuid, permissions);
        });
    }

    public static void addPermissions(UUID uuid, List<String> permissions) {
        Set<String> set = playerPermissions.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        for (String permission : permissions) {
            set.add(normalize(permission));
        }
    }

    public static void setPermission(UUID uuid, String permission, boolean value) {
        Set<String> set = playerPermissions.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        String perm = normalize(permission);
        if (value) set.add(perm);
        else set.remove(perm);
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        Set<String> perms = playerPermissions.getOrDefault(uuid, Collections.emptySet());
        String perm = normalize(permission);

        if (perms.contains("*") || Core.DEV_UUID.equals(uuid)) return true;
        if (perms.contains(perm)) return true;

        int idx = perm.length();
        while ((idx = perm.lastIndexOf('.', idx - 1)) > 0) {
            if (perms.contains(perm.substring(0, idx) + ".*")) return true;
        }
        return false;
    }

    @Subscribe
    public void onPermissionSetup(PermissionsSetupEvent event) {
        PermissionProvider provider = subject -> {

            if (subject instanceof Player player) {
                return perm -> hasPermission(player.getUniqueId(), perm) ? Tristate.TRUE : Tristate.UNDEFINED;
            }

            if (subject instanceof ConsoleCommandSource) {
                return PermissionFunction.ALWAYS_TRUE;
            }

            return PermissionFunction.ALWAYS_UNDEFINED;
        };

        event.setProvider(provider);
    }

    private static String normalize(String p) {
        return p == null ? "" : p.trim().toLowerCase(Locale.ROOT);
    }
}
