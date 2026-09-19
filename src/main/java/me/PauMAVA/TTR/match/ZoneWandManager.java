package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de la Varita de Zonas Administrativa (/dt wand).
 * Incluye delimitador 3D con bounding box de partículas en tiempo real,
 * hologramas efímeros TextDisplay sobre cofres y HUD de volumen en Action Bar.
 */
public class ZoneWandManager {

    private static ZoneWandManager instance;
    public static final NamespacedKey KEY_WAND = new NamespacedKey(TTRCore.getInstance(), "ttr_zone_wand");

    private final Map<UUID, Location> pos1Map = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pos2Map = new ConcurrentHashMap<>();
    private int visualTaskId = -1;

    public static ZoneWandManager getInstance() {
        if (instance == null) {
            instance = new ZoneWandManager();
            instance.startVisualTask();
        }
        return instance;
    }

    public ItemStack createWandItem() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "⚡ " + TextUtil.toTiny("Vara de Zonas Destiny"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + TextUtil.toTiny("Herramienta administrativa con delimitador 3D"));
            lore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            lore.add(ChatColor.YELLOW + "» " + ChatColor.BOLD + TextUtil.toTiny("Clic Izquierdo:") + ChatColor.WHITE + " Marcar Punto 1 (Pos1)");
            lore.add(ChatColor.AQUA + "» " + ChatColor.BOLD + TextUtil.toTiny("Clic Derecho:") + ChatColor.WHITE + " Marcar Punto 2 (Pos2)");
            lore.add(ChatColor.GOLD + "» " + ChatColor.BOLD + TextUtil.toTiny("Shift + Clic Der en Cofre:") + ChatColor.WHITE + " Alternar equipo (Rojo/Azul/Neutro)");
            lore.add(ChatColor.LIGHT_PURPLE + "» " + ChatColor.BOLD + TextUtil.toTiny("Clic Der en Cofre:") + ChatColor.WHITE + " Inspeccionar equipo del cofre");
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
        spawnSingleBlockOutline(loc, Particle.FLAME);
    }

    public void setPos2(Player player, Location loc) {
        pos2Map.put(player.getUniqueId(), loc);
        player.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.AQUA + TextUtil.toTiny("Punto 2 (Pos2) fijado en: ") +
                ChatColor.WHITE + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        spawnSingleBlockOutline(loc, Particle.HAPPY_VILLAGER);
    }

    public Location getPos1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }

    private void startVisualTask() {
        if (visualTaskId != -1) return;

        visualTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isOnline()) continue;
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (!isWand(mainHand)) continue;

                    Location pos1 = pos1Map.get(player.getUniqueId());
                    Location pos2 = pos2Map.get(player.getUniqueId());

                    if (pos1 != null && pos2 != null && pos1.getWorld() != null && pos1.getWorld().equals(pos2.getWorld())) {
                        renderBoundingBox(pos1, pos2);

                        // Actualizar Action Bar con métricas de la selección
                        int dx = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
                        int dy = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
                        int dz = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
                        long volume = (long) dx * dy * dz;

                        String hud = TextUtil.color("&#FFAA00§lVARITA &#888888▪ &#FFFF55Pos1: &#FFFFFF" + pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ() +
                                " &#888888▪ &#55FFFFPos2: &#FFFFFF" + pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ() +
                                " &#888888▪ &#55FF55Volumen: &#FFFFFF" + volume + " bloques");
                        player.sendActionBar(net.kyori.adventure.text.Component.text(hud));
                    } else if (pos1 != null) {
                        String hud = TextUtil.color("&#FFAA00§lVARITA &#888888▪ &#FFFF55Pos1 fijado &#888888▪ &#AAAAAAHaz clic derecho para fijar Pos2");
                        player.sendActionBar(net.kyori.adventure.text.Component.text(hud));
                    }
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 10L).getTaskId();
    }

    private void renderBoundingBox(Location p1, Location p2) {
        World world = p1.getWorld();
        if (world == null) return;

        double minX = Math.min(p1.getBlockX(), p2.getBlockX());
        double maxX = Math.max(p1.getBlockX(), p2.getBlockX()) + 1.0;
        double minY = Math.min(p1.getBlockY(), p2.getBlockY());
        double maxY = Math.max(p1.getBlockY(), p2.getBlockY()) + 1.0;
        double minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        double maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ()) + 1.0;

        // Trazar aristas del cubo en 3D
        double step = 1.0;
        Particle particle = Particle.FLAME;

        for (double x = minX; x <= maxX; x += step) {
            world.spawnParticle(particle, x, minY, minZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, x, maxY, minZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, x, minY, maxZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, x, maxY, maxZ, 1, 0, 0, 0, 0);
        }
        for (double y = minY; y <= maxY; y += step) {
            world.spawnParticle(particle, minX, y, minZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, maxX, y, minZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, minX, y, maxZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, maxX, y, maxZ, 1, 0, 0, 0, 0);
        }
        for (double z = minZ; z <= maxZ; z += step) {
            world.spawnParticle(particle, minX, minY, z, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, maxX, minY, z, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, minX, maxY, z, 1, 0, 0, 0, 0);
            world.spawnParticle(particle, maxX, maxY, z, 1, 0, 0, 0, 0);
        }
    }

    public void spawnSingleBlockOutline(Location loc, Particle particle) {
        if (loc.getWorld() == null) return;
        for (double x = 0; x <= 1.0; x += 0.5) {
            for (double y = 0; y <= 1.0; y += 0.5) {
                for (double z = 0; z <= 1.0; z += 0.5) {
                    loc.getWorld().spawnParticle(particle, loc.getX() + x, loc.getY() + y, loc.getZ() + z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    public void handleChestClick(Player player, org.bukkit.block.Block clickedBlock, boolean isSneaking) {
        if (clickedBlock == null) return;
        Location loc = clickedBlock.getLocation();
        String currentTeam = TTRCore.getInstance().getConfigManager().getTeamForChest(loc);

        if (isSneaking) {
            String newTeam;
            if (currentTeam == null) {
                newTeam = "Red";
            } else if (currentTeam.equalsIgnoreCase("Red")) {
                newTeam = "Blue";
            } else {
                newTeam = null;
            }

            if (newTeam != null) {
                TTRCore.getInstance().getConfigManager().toggleTeamChest(newTeam, loc);
                ChatColor color = newTeam.equalsIgnoreCase("Red") ? ChatColor.RED : ChatColor.BLUE;
                player.sendMessage(TTRPrefix.TTR_ADMIN + TextUtil.toTiny("Cofre asignado al equipo: ") + color + "" + ChatColor.BOLD + TextUtil.toTiny(newTeam) +
                        ChatColor.GRAY + " (" + TextUtil.toTiny("Protegido contra enemigos") + ")");
                player.playSound(loc, Sound.BLOCK_COPPER_BULB_TURN_ON, 1f, newTeam.equalsIgnoreCase("Red") ? 1.0f : 1.5f);
                spawnSingleBlockOutline(loc, newTeam.equalsIgnoreCase("Red") ? Particle.FLAME : Particle.SOUL_FIRE_FLAME);
                showFloatingChestHologram(loc, (newTeam.equalsIgnoreCase("Red") ? "§c§l[COFRE ROJO]" : "§9§l[COFRE AZUL]"));
            } else {
                TTRCore.getInstance().getConfigManager().toggleTeamChest("Red", loc);
                TTRCore.getInstance().getConfigManager().toggleTeamChest("Blue", loc);
                player.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Cofre desasignado (Neutro / Libre)."));
                player.playSound(loc, Sound.BLOCK_COPPER_BULB_TURN_OFF, 1f, 1.0f);
                spawnSingleBlockOutline(loc, Particle.SMOKE);
                showFloatingChestHologram(loc, "§e§l[COFRE NEUTRO]");
            }
        } else {
            if (currentTeam != null) {
                ChatColor color = currentTeam.equalsIgnoreCase("Red") ? ChatColor.RED : ChatColor.BLUE;
                player.sendMessage(TTRPrefix.TTR_ADMIN + TextUtil.toTiny("Estado del cofre: Asignado a ") + color + "" + ChatColor.BOLD + TextUtil.toTiny(currentTeam) +
                        ChatColor.GRAY + " (" + TextUtil.toTiny("Shift + Clic para cambiar") + ")");
                showFloatingChestHologram(loc, color + "§l[COFRE " + currentTeam.toUpperCase() + "]");
            } else {
                player.sendMessage(TTRPrefix.TTR_ADMIN + TextUtil.toTiny("Estado del cofre: ") + ChatColor.YELLOW + TextUtil.toTiny("Neutro / Libre") +
                        ChatColor.GRAY + " (" + TextUtil.toTiny("Shift + Clic para asignar a Rojo") + ")");
                showFloatingChestHologram(loc, "§e§l[COFRE NEUTRO]");
            }
            player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1.2f);
        }
    }

    private void showFloatingChestHologram(Location blockLoc, String text) {
        World world = blockLoc.getWorld();
        if (world == null) return;

        Location holoLoc = blockLoc.clone().add(0.5, 1.2, 0.5);
        try {
            TextDisplay display = world.spawn(holoLoc, TextDisplay.class, entity -> {
                entity.text(net.kyori.adventure.text.Component.text(text));
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setDefaultBackground(false);
                entity.setBackgroundColor(Color.fromARGB(150, 0, 0, 0));
                entity.setBrightness(new Display.Brightness(15, 15));
            });

            // Autodestrucción en 3 segundos (60 ticks)
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (display.isValid()) {
                        display.remove();
                    }
                }
            }.runTaskLater(TTRCore.getInstance(), 60L);
        } catch (Throwable ignored) {}
    }
}
