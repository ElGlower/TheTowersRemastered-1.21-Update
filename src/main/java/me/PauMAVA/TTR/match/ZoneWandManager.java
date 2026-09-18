package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de la Varita de Zonas Administrativa (/dt wand).
 * Permite marcar Pos1 y Pos2 con partículas visuales para definir fácilmente
 * el lobby, celdas de spawn y jaulas de anotación.
 */
public class ZoneWandManager {

    private static ZoneWandManager instance;
    public static final NamespacedKey KEY_WAND = new NamespacedKey(TTRCore.getInstance(), "ttr_zone_wand");

    private final Map<UUID, Location> pos1Map = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pos2Map = new ConcurrentHashMap<>();

    public static ZoneWandManager getInstance() {
        if (instance == null) {
            instance = new ZoneWandManager();
        }
        return instance;
    }

    public ItemStack createWandItem() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "⚡ " + TextUtil.toTiny("Vara de Zonas Destiny"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + TextUtil.toTiny("Herramienta administrativa de delimitación"));
            lore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            lore.add(ChatColor.YELLOW + "» " + ChatColor.BOLD + TextUtil.toTiny("Clic Izquierdo:") + ChatColor.WHITE + " Marcar Punto 1 (Pos1)");
            lore.add(ChatColor.AQUA + "» " + ChatColor.BOLD + TextUtil.toTiny("Clic Derecho:") + ChatColor.WHITE + " Marcar Punto 2 (Pos2)");
            lore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            lore.add(ChatColor.GREEN + TextUtil.toTiny("Comandos útiles:"));
            lore.add(ChatColor.WHITE + "/dt wand setspawn <red|blue>");
            lore.add(ChatColor.WHITE + "/dt wand setcage <red|blue>");
            lore.add(ChatColor.WHITE + "/dt wand setbase <red|blue>");
            lore.add(ChatColor.WHITE + "/dt wand setlobby");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(KEY_WAND, PersistentDataType.BYTE, (byte) 1);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_WAND, PersistentDataType.BYTE);
    }

    public void giveWand(Player player) {
        player.getInventory().addItem(createWandItem());
        player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Has recibido la ") + 
                ChatColor.GOLD + TextUtil.toTiny("Vara de Zonas Destiny") + ChatColor.GREEN + ".");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
    }

    public void setPos1(Player player, Location loc) {
        pos1Map.put(player.getUniqueId(), loc);
        player.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Punto 1 (Pos1) fijado en: ") +
                ChatColor.WHITE + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        spawnOutlineParticles(player, loc, Particle.FLAME);
    }

    public void setPos2(Player player, Location loc) {
        pos2Map.put(player.getUniqueId(), loc);
        player.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.AQUA + TextUtil.toTiny("Punto 2 (Pos2) fijado en: ") +
                ChatColor.WHITE + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        spawnOutlineParticles(player, loc, Particle.HAPPY_VILLAGER);
    }

    public Location getPos1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }

    public void spawnOutlineParticles(Player player, Location loc, Particle particle) {
        if (loc.getWorld() == null) return;
        for (double x = 0; x <= 1.0; x += 0.5) {
            for (double y = 0; y <= 1.0; y += 0.5) {
                for (double z = 0; z <= 1.0; z += 0.5) {
                    loc.getWorld().spawnParticle(particle, loc.getX() + x, loc.getY() + y, loc.getZ() + z, 1, 0, 0, 0, 0);
                }
            }
        }
    }
}
