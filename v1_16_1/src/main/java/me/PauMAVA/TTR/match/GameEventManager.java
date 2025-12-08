package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class GameEventManager {

    private boolean autoEvents = true;
    private final Random random = new Random();

    public void startCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!TTRCore.getInstance().getCurrentMatch().isOnCourse()) { this.cancel(); return; }
                if (autoEvents) startRandomEvent();
            }
        }.runTaskTimer(TTRCore.getInstance(), 3600L, 3600L); // 3 minutos
    }

    public void startRandomEvent() {
        int event = random.nextInt(6);
        switch (event) {
            case 0: triggerPotionEvent("Gravedad Lunar", PotionEffectType.JUMP_BOOST, 2); break;
            case 1: triggerPotionEvent("Flash", PotionEffectType.SPEED, 2); break;
            case 2: triggerPotionEvent("Tinieblas", PotionEffectType.BLINDNESS, 0); break;
            case 3: triggerPotionEvent("Caída de Pluma", PotionEffectType.SLOW_FALLING, 0); break;
            case 4: triggerSizeEvent(); break;
            case 5: triggerMeteorShower(); break;
        }
    }

    public void triggerPotionEvent(String name, PotionEffectType type, int amplifier) {
        broadcastEvent(name);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.addPotionEffect(new PotionEffect(type, 30 * 20, amplifier));
        }
    }

    private void triggerSizeEvent() {
        broadcastEvent("Titanes y Hormigas");
        for (Player p : Bukkit.getOnlinePlayers()) {
            // 50% probabilidad: Pequeño (0.5) o Gigante (1.5)
            double scale = random.nextBoolean() ? 0.5 : 1.5;
            if (p.getAttribute(Attribute.GENERIC_SCALE) != null) {
                p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(scale);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getAttribute(Attribute.GENERIC_SCALE) != null) {
                        p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);
                    }
                }
            }
        }.runTaskLater(TTRCore.getInstance(), 600L);
    }

    private void triggerMeteorShower() {
        broadcastEvent("Lluvia de Meteoritos");
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 15) { this.cancel(); return; }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (random.nextBoolean()) {
                        Location loc = p.getLocation().add(random.nextInt(10) - 5, 20, random.nextInt(10) - 5);
                        Fireball fb = p.getWorld().spawn(loc, Fireball.class);
                        fb.setDirection(new org.bukkit.util.Vector(0, -1, 0));
                        fb.setYield(0); // No rompe bloques
                        fb.setIsIncendiary(false);
                    }
                }
                count++;
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 20L);
    }

    public void triggerManual(String type) {
        switch (type.toLowerCase()) {
            case "size": triggerSizeEvent(); break;
            case "meteors": triggerMeteorShower(); break;
            case "fall": triggerPotionEvent("Caída de Pluma", PotionEffectType.SLOW_FALLING, 0); break;
        }
    }

    private void broadcastEvent(String name) {
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + " ¡EVENTO: " + ChatColor.GOLD + name + ChatColor.LIGHT_PURPLE + "! ");
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1, 0.5f);
    }

    public void setAutoEvents(boolean active) { this.autoEvents = active; }
}