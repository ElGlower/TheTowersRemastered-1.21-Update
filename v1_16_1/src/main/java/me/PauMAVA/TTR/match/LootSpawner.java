package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.*;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class LootSpawner {

    private int taskId;

    public void startSpawning() {
        this.taskId = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (TTRCore.getInstance().getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                    this.cancel();
                    return;
                }

                ticks++;

                // HIERRO (Cada 2 segundos = 40 ticks)
                if (ticks % 40 == 0) {
                    spawnItems("iron", Material.IRON_INGOT);
                }

                // CARBÓN (Cada 40 segundos = 800 ticks)
                if (ticks % 800 == 0) {
                    spawnItems("coal", Material.COAL);
                }

                // XP (Cada 15 segundos = 300 ticks)
                if (ticks % 300 == 0) {
                    spawnXP();
                }

                // ESMERALDA (Cada 20 segundos = 200 ticks)
                if (ticks % 200 == 0) {
                    spawnItems("emerald", Material.EMERALD);
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 1L).getTaskId();
    }

    public void stopSpawning() {
        Bukkit.getScheduler().cancelTask(this.taskId);
    }

    private void spawnItems(String type, Material mat) {
        List<Location> locs = TTRCore.getInstance().getConfigManager().getSpawns(type);
        if (locs == null || locs.isEmpty()) return;

        for (Location loc : locs) {
            if (loc != null && loc.getWorld() != null) {
                Item item = loc.getWorld().dropItem(loc, new ItemStack(mat));
                item.setVelocity(new Vector(0, 0.1, 0)); // Pequeño salto hacia arriba

                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 5, 0.2, 0.2, 0.2);
                loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 0.5f, 1.5f);
            }
        }
    }

    private void spawnXP() {
        List<Location> locs = TTRCore.getInstance().getConfigManager().getSpawns("xp");
        if (locs == null || locs.isEmpty()) return;

        for (Location loc : locs) {
            if (loc != null && loc.getWorld() != null) {
                ExperienceOrb orb = (ExperienceOrb) loc.getWorld().spawn(loc, ExperienceOrb.class);
                orb.setExperience(5);

                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 10, 0.2, 0.5, 0.2);
                loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
            }
        }
    }
}