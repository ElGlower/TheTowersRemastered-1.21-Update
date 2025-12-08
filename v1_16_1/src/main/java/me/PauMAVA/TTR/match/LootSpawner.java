/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 * [Licencia...]
 */

package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class LootSpawner {

    private List<Location> ironLocations;
    private List<Location> xpLocations;
    private long ironFrequency = 150;
    private long xpFrequency = 150;
    private int ironTaskPID;
    private int xpTaskPID;

    public LootSpawner() {
        this.ironLocations = TTRCore.getInstance().getConfigManager().getIronSpawns();
        this.xpLocations = TTRCore.getInstance().getConfigManager().getXPSpawns();
    }

    public void startSpawning() {
        setIronTask();
        setXpTask();
    }

    public void stopSpawning() {
        Bukkit.getScheduler().cancelTask(this.ironTaskPID);
        Bukkit.getScheduler().cancelTask(this.xpTaskPID);
    }

    private void setIronTask() {
        this.ironTaskPID = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location location : ironLocations) {
                    Location copy = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                    copy.add(location.getX() > 0 ? 0.5 : 0.5, 0.0, location.getZ() > 0 ? 0.5 : -0.5);
                    location.getWorld().dropItem(copy, new ItemStack(Material.IRON_INGOT, 1));
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, this.ironFrequency).getTaskId();
    }

    private void setXpTask() {
        this.xpTaskPID = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location location : xpLocations) {
                    Location copy = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                    copy.add(location.getX() > 0 ? 0.5 : 0.5, 0.0, location.getZ() > 0 ? 0.5 : -0.5);
                    // THROWN_EXP_BOTTLE cambió de nombre a EXPERIENCE_BOTTLE
                    location.getWorld().spawnEntity(copy, EntityType.EXPERIENCE_BOTTLE);
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, this.xpFrequency).getTaskId();
    }

}