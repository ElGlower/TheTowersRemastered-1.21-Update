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

    private int taskId = -1;

    public void startSpawning() {
        stopSpawning();
        this.taskId = new BukkitRunnable() {
            int seconds = 0;

            @Override
            public void run() {
                if (TTRCore.getInstance().getCurrentMatch() == null ||
                    TTRCore.getInstance().getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                    this.cancel();
                    return;
                }

                seconds++;

                // HIERRO (Cada 2 segundos)
                if (seconds % 2 == 0) {
                    spawnItems("iron", Material.IRON_INGOT);
                }

                // XP (Cada 15 segundos)
                if (seconds % 15 == 0) {
                    spawnXP();
                }

                // ESMERALDA (Cada 20 segundos)
                if (seconds % 20 == 0) {
                    spawnItems("emerald", Material.EMERALD);
                }

                // CARBÓN (Cada 40 segundos)
                if (seconds % 40 == 0) {
                    spawnItems("coal", Material.COAL);
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 20L).getTaskId();
    }

    public void stopSpawning() {
        if (this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }
    }

    private void spawnItems(String type, Material mat) {
        List<Location> locs = TTRCore.getInstance().getConfigManager().getSpawns(type);
        if (locs == null || locs.isEmpty()) return;

        for (Location loc : locs) {
            if (loc != null && loc.getWorld() != null) {
                // Verificar que el chunk esté cargado para no forzar cargas sincrónicas
                if (!loc.isChunkLoaded()) continue;

                // Evitar acumulación masiva de entidades en el mismo spawner (máx 32 ítems)
                int existing = 0;
                for (org.bukkit.entity.Entity e : loc.getWorld().getNearbyEntities(loc, 2.0, 2.0, 2.0)) {
                    if (e instanceof Item itemEntity && itemEntity.getItemStack().getType() == mat) {
                        existing += itemEntity.getItemStack().getAmount();
                        if (existing >= 32) break;
                    }
                }
                if (existing >= 32) continue;

                Item item = loc.getWorld().dropItem(loc, new ItemStack(mat));
                item.setVelocity(new Vector(0, 0.1, 0)); // Pequeño salto hacia arriba

                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 3, 0.2, 0.2, 0.2);
                loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 0.4f, 1.5f);
            }
        }
    }

    private void spawnXP() {
        List<Location> locs = TTRCore.getInstance().getConfigManager().getSpawns("xp");
        if (locs == null || locs.isEmpty()) return;

        for (Location loc : locs) {
            if (loc != null && loc.getWorld() != null) {
                if (!loc.isChunkLoaded()) continue;

                // Evitar acumulación de orbes de experiencia
                int nearbyOrbs = 0;
                for (org.bukkit.entity.Entity e : loc.getWorld().getNearbyEntities(loc, 2.5, 2.5, 2.5)) {
                    if (e instanceof ExperienceOrb) {
                        nearbyOrbs++;
                        if (nearbyOrbs >= 5) break;
                    }
                }
                if (nearbyOrbs >= 5) continue;

                ExperienceOrb orb = (ExperienceOrb) loc.getWorld().spawn(loc, ExperienceOrb.class);
                orb.setExperience(5);

                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 5, 0.2, 0.4, 0.2);
                loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 1.0f);
            }
        }
    }
}