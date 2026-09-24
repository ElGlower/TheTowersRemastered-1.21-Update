package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private final String[] events = {"jump", "speed", "blind", "meteors", "giga", "mini", "wind", "gravity"};
    private String currentEvent = null;

    public String getCurrentEvent() {
        return currentEvent;
    }

    public void toggleAutoMode(boolean enable) {
        this.autoMode = enable;
        if (enable) {
            startCycle();
            Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.GREEN + 
                    TextUtil.toTiny("Eventos automáticos ACTIVADOS."));
        } else {
            stopCycle();
            stopCurrentEvent();
            Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.RED + 
                    TextUtil.toTiny("Eventos automáticos DESACTIVADOS."));
        }
    }

    public boolean isAutoMode() { return autoMode; }

    public void startCycle() {
        stopCycle();

        cycleTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (!autoMode) { this.cancel(); return; }
                if (TTRCore.getInstance().getCurrentMatch() != null && 
                    TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
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
        stopCurrentEvent();
        this.currentEvent = eventName;

        int playingCount = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlaying(p)) playingCount++;
        }
        if (playingCount == 0) {
            return;
        }

        String tinyName = TextUtil.toTiny(eventName.toUpperCase());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlaying(p)) {
                p.sendMessage(ChatColor.DARK_AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "¡" + TextUtil.toTiny("EVENTO: ") + tinyName + "!");
                p.sendMessage(ChatColor.YELLOW + TextUtil.toTiny("Duración: 60 segundos"));
                p.sendMessage(ChatColor.DARK_AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                p.sendTitle(ChatColor.GOLD + "¡" + tinyName + "!", ChatColor.YELLOW + TextUtil.toTiny("Evento de caos activado"), 10, 40, 10);
                p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1f, 0.5f);
            }
        }

        switch (eventName.toLowerCase()) {
            case "jump":
                applyEffectToAll(PotionEffectType.JUMP_BOOST, 4);
                break;
            case "speed":
                applyEffectToAll(PotionEffectType.SPEED, 3);
                break;
            case "blind":
                applyEffectToAll(PotionEffectType.BLINDNESS, 0);
                applyEffectToAll(PotionEffectType.DARKNESS, 0);
                break;
            case "giga":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlaying(p)) {
                        setSafeAttribute(p, Attribute.SCALE, 2.0);
                        setSafeAttribute(p, Attribute.MAX_HEALTH, 40.0);
                        p.setHealth(40.0);
                    }
                }
                break;
            case "mini":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlaying(p)) {
                        setSafeAttribute(p, Attribute.SCALE, 0.5);
                        if (p.getHealth() > 10.0) p.setHealth(10.0);
                        setSafeAttribute(p, Attribute.MAX_HEALTH, 10.0);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 2));
                    }
                }
                break;
            case "wind":
                // Evento 1.21: Cargas de viento para los jugadores de la partida
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlaying(p)) {
                        p.getInventory().addItem(new ItemStack(Material.WIND_CHARGE, 3));
                        p.playSound(p.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1f, 1f);
                    }
                }
                break;
            case "gravity":
                // Gravedad Lunar: Salto + Caída lenta
                applyEffectToAll(PotionEffectType.JUMP_BOOST, 3);
                applyEffectToAll(PotionEffectType.SLOW_FALLING, 0);
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
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlaying(p)) {
                        p.sendMessage(TTRPrefix.TTR_GAME + ChatColor.GREEN + 
                                TextUtil.toTiny("El evento ") + ChatColor.YELLOW + tinyName + 
                                ChatColor.GREEN + TextUtil.toTiny(" ha terminado."));
                    }
                }
            }
        }.runTaskLater(TTRCore.getInstance(), 1200L).getTaskId();
    }

    public void stopCurrentEvent() {
        if (stopTaskID != -1) { Bukkit.getScheduler().cancelTask(stopTaskID); stopTaskID = -1; }
        if (actionTaskID != -1) { Bukkit.getScheduler().cancelTask(actionTaskID); actionTaskID = -1; }
        this.currentEvent = null;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.SPEED);
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.DARKNESS);
            p.removePotionEffect(PotionEffectType.SLOW_FALLING);

            setSafeAttribute(p, Attribute.SCALE, 1.0);

            if (p.getAttribute(Attribute.MAX_HEALTH) != null) {
                setSafeAttribute(p, Attribute.MAX_HEALTH, 20.0);
            }
            if (p.getHealth() > 20.0) p.setHealth(20.0);
        }
    }

    private boolean isPlaying(Player p) {
        if (p == null || !p.isOnline()) return false;
        if (TTRCore.getInstance().getCurrentMatch() == null || 
            TTRCore.getInstance().getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            return false;
        }
        if (p.getGameMode() != GameMode.SURVIVAL) return false;
        if (TTRCore.getInstance().getTeamHandler().getPlayerTeam(p) == null) return false;
        String w = p.getWorld().getName().toLowerCase();
        if (w.equals("lobby") || w.equals("world")) return false;
        return true;
    }

    private void applyEffectToAll(PotionEffectType type, int amp) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlaying(p)) {
                p.addPotionEffect(new PotionEffect(type, 1200, amp));
            }
        }
    }

    private void setSafeAttribute(Player p, Attribute attr, double value) {
        AttributeInstance instance = p.getAttribute(attr);
        if (instance != null) {
            for (AttributeModifier mod : instance.getModifiers()) {
                instance.removeModifier(mod);
            }
            instance.setBaseValue(value);
        }
    }

    private void startMeteorShower() {
        actionTaskID = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 20 || TTRCore.getInstance().getCurrentMatch() == null || 
                    TTRCore.getInstance().getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                    this.cancel();
                    return;
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isPlaying(p)) continue;

                    Location loc = p.getLocation().add(0, 20, 0);
                    loc.add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);

                    Fireball fb = p.getWorld().spawn(loc, Fireball.class);
                    fb.setDirection(new org.bukkit.util.Vector(0, -1, 0));
                    fb.setYield(2.0F);
                }
                count++;
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 60L).getTaskId();
    }
}
