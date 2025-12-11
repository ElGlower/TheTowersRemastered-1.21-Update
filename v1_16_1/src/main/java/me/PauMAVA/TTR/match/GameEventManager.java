package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class GameEventManager {

    private boolean autoMode = true;
    private int cycleTaskID = -1;
    private int stopTaskID = -1;
    private int actionTaskID = -1;

    private final Random random = new Random();
    private final String[] events = {"jump", "speed", "blind", "meteors", "giga", "mini"};

    public void toggleAutoMode(boolean enable) {
        this.autoMode = enable;
        if (enable) {
            startCycle();
            Bukkit.broadcastMessage(ChatColor.GOLD + "[TTR] " + ChatColor.GREEN + "Eventos automáticos ACTIVADOS.");
        } else {
            stopCycle();
            stopCurrentEvent();
            Bukkit.broadcastMessage(ChatColor.GOLD + "[TTR] " + ChatColor.RED + "Eventos automáticos DESACTIVADOS.");
        }
    }

    public boolean isAutoMode() { return autoMode; }

    public void startCycle() {
        if (cycleTaskID != -1) Bukkit.getScheduler().cancelTask(cycleTaskID);

        // Cada 3 minutos (3600 ticks) intenta lanzar un evento
        cycleTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (!autoMode) { this.cancel(); return; }
                // Solo si la partida está en curso
                if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
                    triggerRandomEvent();
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 1200L, 3600L).getTaskId();
    }

    public void stopCycle() {
        if (cycleTaskID != -1) {
            Bukkit.getScheduler().cancelTask(cycleTaskID);
            cycleTaskID = -1;
        }
    }

    public void triggerRandomEvent() {
        String event = events[random.nextInt(events.length)];
        triggerEvent(event);
    }

    public void triggerEvent(String eventName) {
        stopCurrentEvent(); // Limpiar el anterior para evitar caos

        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "¡EVENTO: " + eventName.toUpperCase() + "!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Duración: 60 segundos");
        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1, 0.5f);
        }

        switch (eventName.toLowerCase()) {
            case "jump":
                applyEffectToAll(PotionEffectType.JUMP_BOOST, 5);
                break;
            case "speed":
                applyEffectToAll(PotionEffectType.SPEED, 4);
                break;
            case "blind":
                applyEffectToAll(PotionEffectType.BLINDNESS, 0);
                applyEffectToAll(PotionEffectType.DARKNESS, 0);
                break;
            case "giga":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlaying(p)) {
                        setSafeAttribute(p, Attribute.GENERIC_SCALE, 2.0);
                        setSafeAttribute(p, Attribute.GENERIC_MAX_HEALTH, 40.0);
                        p.setHealth(40.0);
                    }
                }
                break;
            case "mini":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlaying(p)) {
                        setSafeAttribute(p, Attribute.GENERIC_SCALE, 0.5);
                        if (p.getHealth() > 10.0) p.setHealth(10.0);
                        setSafeAttribute(p, Attribute.GENERIC_MAX_HEALTH, 10.0);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 2));
                    }
                }
                break;
            case "meteors":
                startMeteorShower();
                break;
        }

        if (stopTaskID != -1) Bukkit.getScheduler().cancelTask(stopTaskID);
        stopTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                stopCurrentEvent();
                Bukkit.broadcastMessage(ChatColor.GREEN + "El evento " + eventName + " ha terminado.");
            }
        }.runTaskLater(TTRCore.getInstance(), 1200L).getTaskId();
    }

    public void stopCurrentEvent() {
        if (stopTaskID != -1) { Bukkit.getScheduler().cancelTask(stopTaskID); stopTaskID = -1; }
        if (actionTaskID != -1) { Bukkit.getScheduler().cancelTask(actionTaskID); actionTaskID = -1; }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.SPEED);
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.DARKNESS);

            setSafeAttribute(p, Attribute.GENERIC_SCALE, 1.0);

            if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                setSafeAttribute(p, Attribute.GENERIC_MAX_HEALTH, 20.0);
            }
            if (p.getHealth() > 20.0) p.setHealth(20.0);
        }
    }

    private boolean isPlaying(Player p) {
        return p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE;
    }

    private void applyEffectToAll(PotionEffectType type, int amp) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlaying(p)) {
                p.addPotionEffect(new PotionEffect(type, 1200, amp));
            }
        }
    }

    private void setSafeAttribute(Player p, Attribute attr, double value) {
        if (p.getAttribute(attr) != null) {
            for (AttributeModifier mod : p.getAttribute(attr).getModifiers()) {
                p.getAttribute(attr).removeModifier(mod);
            }
            p.getAttribute(attr).setBaseValue(value);
        }
    }

    private void startMeteorShower() {
        actionTaskID = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 20) { this.cancel(); return; } // 20 oleadas

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isPlaying(p)) continue;

                    Location loc = p.getLocation().add(0, 20, 0);
                    loc.add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);

                    Fireball fb = p.getWorld().spawn(loc, Fireball.class);
                    fb.setDirection(new org.bukkit.util.Vector(0, -1, 0));
                    fb.setYield(2.0F); // Explosión
                }
                count++;
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 60L).getTaskId(); // Cada 3 segundos
    }
}