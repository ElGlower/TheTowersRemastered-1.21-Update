package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class LootSpawner {

    private int taskID;

    public void startSpawning() {
        this.taskID = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;

                // Hierro (Cada 5 segundos)
                if (ticks % 100 == 0) spawnItem("iron", Material.IRON_INGOT);

                // XP (Cada 15 segundos)
                if (ticks % 300 == 0) spawnItem("xp", Material.EXPERIENCE_BOTTLE);

                // Carbón (Cada 10 segundos)
                if (ticks % 200 == 0) spawnItem("coal", Material.COAL);

                // Esmeralda (Cada 60 segundos)
                if (ticks % 1200 == 0) spawnItem("emerald", Material.EMERALD);
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 1L).getTaskId();
    }

    private void spawnItem(String type, Material mat) {
        List<Location> locs = TTRCore.getInstance().getConfigManager().getSpawns(type);
        if (locs == null) return;
        for (Location loc : locs) {
            if (loc != null && loc.getWorld() != null) {
                loc.getWorld().dropItemNaturally(loc.add(0, 1, 0), new ItemStack(mat));
            }
        }
    }

    public void stopSpawning() {
        try {
            org.bukkit.Bukkit.getScheduler().cancelTask(this.taskID);
        } catch (Exception ignored) {}
    }
}