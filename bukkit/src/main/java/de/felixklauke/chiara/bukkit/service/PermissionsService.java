package de.felixklauke.chiara.bukkit.service;

import org.bukkit.entity.Player;

public interface PermissionsService {

    /**
     * Register the given player and calculate his permissions.
     *
     * @param player The player.
     */
    void registerPlayer(Player player);

    /**
     * Unregister the given player and delete his permissions.
     *
     * @param player The player.
     */
    void unregisterPlayer(Player player);

    /**
     * Refresh the permissions of the given player.
     *
     * @param player The player.
     */
    void refreshPlayer(Player player);

    /**
     * Reload all permissions.
     */
    void reloadPermissions();

    /**
     * Get the names of all groups.
     *
     * @return The names of the groups.
     */
    String[] getGroups();
}
