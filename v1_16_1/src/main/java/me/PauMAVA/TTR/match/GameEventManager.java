package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class GameEventManager {

    private boolean autoEvents = true;
    private BukkitRunnable currentTask;
    private final Random random = new Random();

    public void startCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!TTRCore.getInstance().getCurrentMatch().isOnCourse()) {
                    this.cancel();
                    return;
                }
                if (autoEvents) {
                    startRandomEvent();
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 3600L, 3600L);
    }

    public void startRandomEvent() {
        int event = random.nextInt(4);
        switch (event) {
            case 0:
                triggerEvent("Gravedad Lunar", PotionEffectType.JUMP_BOOST, 2);
                break;
            case 1:
                triggerEvent("Flash", PotionEffectType.SPEED, 2);
                break;
            case 2:
                triggerEvent("Tinieblas", PotionEffectType.BLINDNESS, 0);
                break;
            case 3:
                triggerEvent("Pesadez", PotionEffectType.SLOWNESS, 1);
                break;
        }
    }

    public void triggerEvent(String name, PotionEffectType type, int amplifier) {
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + " ¡EVENTO ALEATORIO: " + ChatColor.GOLD + name + ChatColor.LIGHT_PURPLE + "! ");
        Bukkit.broadcastMessage(ChatColor.GRAY + " Duración: 30 segundos ");
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1, 0.5f);
            p.addPotionEffect(new PotionEffect(type, 30 * 20, amplifier));
        }
    }

    public void setAutoEvents(boolean active) {
        this.autoEvents = active;
    }
}