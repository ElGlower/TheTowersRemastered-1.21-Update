package me.PauMAVA.TTR.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generador y gestor de cabezas de jugadores con soporte para texturas reales
 * mediante PlayerProfile de Paper y resolución de skins (Mojang / NameMC).
 */
public class SkullUtil {

    private static final Map<UUID, PlayerProfile> PROFILE_CACHE = new ConcurrentHashMap<>();

    public static ItemStack getPlayerHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return item;

        if (player.isOnline() && player instanceof Player onlinePlayer) {
            try {
                PlayerProfile profile = onlinePlayer.getPlayerProfile();
                meta.setPlayerProfile(profile);
                item.setItemMeta(meta);
                return item;
            } catch (Exception ignored) {}
        }

        UUID uuid = player.getUniqueId();
        String name = player.getName();

        if (PROFILE_CACHE.containsKey(uuid)) {
            meta.setPlayerProfile(PROFILE_CACHE.get(uuid));
            item.setItemMeta(meta);
            return item;
        }

        try {
            PlayerProfile profile = Bukkit.createProfile(uuid, name != null ? name : uuid.toString().substring(0, 8));
            profile.completeFromCache(true, true);
            meta.setPlayerProfile(profile);
            PROFILE_CACHE.put(uuid, profile);
        } catch (Throwable t) {
            try {
                meta.setOwningPlayer(player);
            } catch (Exception ignored) {}
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getPlayerHead(UUID uuid, String name) {
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        return getPlayerHead(off);
    }
}
