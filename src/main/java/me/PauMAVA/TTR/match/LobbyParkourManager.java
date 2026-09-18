package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor del Parkour del Lobby.
 * Permite a los jugadores entretenerse mientras esperan en el lobby con checkpoints,
 * cronómetro en tiempo real y recuperación automática ante caídas.
 */
public class LobbyParkourManager {

    private static LobbyParkourManager instance;

    private Location startLocation = null;
    private final List<Location> checkpoints = new ArrayList<>();
    private Location endLocation = null;

    // Estado del jugador: UUID -> Datos de Parkour
    private final Map<UUID, Long> startTimeMap = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> currentCheckpointMap = new ConcurrentHashMap<>();

    public static LobbyParkourManager getInstance() {
        if (instance == null) {
            instance = new LobbyParkourManager();
        }
        return instance;
    }

    public LobbyParkourManager() {
        loadFromConfig();
    }

    public void loadFromConfig() {
        FileConfiguration config = TTRCore.getInstance().getConfig();
        checkpoints.clear();

        if (config.contains("lobby_parkour.start")) {
            startLocation = config.getLocation("lobby_parkour.start");
        }
        if (config.contains("lobby_parkour.end")) {
            endLocation = config.getLocation("lobby_parkour.end");
        }

        ConfigurationSection cpSection = config.getConfigurationSection("lobby_parkour.checkpoints");
        if (cpSection != null) {
            for (String key : cpSection.getKeys(false)) {
                Location loc = cpSection.getLocation(key);
                if (loc != null) checkpoints.add(loc);
            }
        }
    }

    public void saveToConfig() {
        FileConfiguration config = TTRCore.getInstance().getConfig();
        config.set("lobby_parkour.start", startLocation);
        config.set("lobby_parkour.end", endLocation);

        config.set("lobby_parkour.checkpoints", null);
        for (int i = 0; i < checkpoints.size(); i++) {
            config.set("lobby_parkour.checkpoints." + i, checkpoints.get(i));
        }
        TTRCore.getInstance().saveConfig();
    }

    public boolean isDoingParkour(Player player) {
        return startTimeMap.containsKey(player.getUniqueId());
    }

    public void startParkour(Player player) {
        startTimeMap.put(player.getUniqueId(), System.currentTimeMillis());
        currentCheckpointMap.put(player.getUniqueId(), 0);

        player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + TextUtil.toTiny("PARKOUR"),
                ChatColor.YELLOW + TextUtil.toTiny("¡Corre hacia la meta!"), 5, 30, 5);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        player.sendMessage(TTRPrefix.TTR_GAME + ChatColor.GREEN + TextUtil.toTiny("¡Parkour iniciado! Toca los checkpoints y llega al final."));
    }

    public void triggerCheckpoint(Player player, int index, Location loc) {
        int current = currentCheckpointMap.getOrDefault(player.getUniqueId(), 0);
        if (index > current) {
            currentCheckpointMap.put(player.getUniqueId(), index);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            player.sendActionBar(Component.text(TextUtil.color("&#55FFFF§l★ " + TextUtil.toTiny("CHECKPOINT #") + index + " " + TextUtil.toTiny("ALCANZADO!"))));
            if (loc.getWorld() != null) {
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3);
            }
        }
    }

    public void finishParkour(Player player) {
        if (!isDoingParkour(player)) return;
        long elapsedMillis = System.currentTimeMillis() - startTimeMap.get(player.getUniqueId());
        double seconds = elapsedMillis / 1000.0;

        startTimeMap.remove(player.getUniqueId());
        currentCheckpointMap.remove(player.getUniqueId());

        String timeFormatted = String.format(Locale.US, "%.2f", seconds);

        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.GOLD + "" + ChatColor.BOLD +
                player.getName() + ChatColor.YELLOW + TextUtil.toTiny(" completó el Parkour del Lobby en ") +
                ChatColor.GREEN + "" + ChatColor.BOLD + timeFormatted + "s" + ChatColor.YELLOW + "!");

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.sendTitle(ChatColor.GOLD + "★ " + TextUtil.toTiny("¡PARKOUR COMPLETADO!") + " ★",
                ChatColor.AQUA + TextUtil.toTiny("Tiempo: ") + ChatColor.WHITE + timeFormatted + "s", 5, 50, 10);

        // Disparar fuego artificial festivo
        Location loc = player.getLocation();
        if (loc.getWorld() != null) {
            Firework fw = loc.getWorld().spawn(loc, Firework.class);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder()
                    .withColor(Color.YELLOW, Color.ORANGE, Color.LIME)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withFlicker()
                    .build());
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
        }
    }

    public void handleFall(Player player) {
        if (!isDoingParkour(player)) return;

        int cp = currentCheckpointMap.getOrDefault(player.getUniqueId(), 0);
        Location respawnLoc = startLocation;
        if (cp > 0 && cp <= checkpoints.size()) {
            respawnLoc = checkpoints.get(cp - 1);
        }

        if (respawnLoc == null) {
            respawnLoc = TTRCore.getInstance().getConfigManager().getLobbyLocation();
        }

        if (respawnLoc != null) {
            player.teleport(respawnLoc);
            player.setVelocity(new Vector(0, 0, 0));
            player.setFallDistance(0f);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
            player.sendActionBar(Component.text(TextUtil.color("&#FF5555" + TextUtil.toTiny("¡Te caíste! Reapareciendo en tu último checkpoint..."))));
        }
    }

    public void cancelParkour(Player player) {
        startTimeMap.remove(player.getUniqueId());
        currentCheckpointMap.remove(player.getUniqueId());
    }

    public void setStartLocation(Location loc) {
        this.startLocation = loc;
        saveToConfig();
    }

    public void addCheckpoint(Location loc) {
        this.checkpoints.add(loc);
        saveToConfig();
    }

    public void setEndLocation(Location loc) {
        this.endLocation = loc;
        saveToConfig();
    }

    public void clearParkour() {
        this.startLocation = null;
        this.endLocation = null;
        this.checkpoints.clear();
        saveToConfig();
    }

    public Location getStartLocation() { return startLocation; }
    public List<Location> getCheckpoints() { return checkpoints; }
    public Location getEndLocation() { return endLocation; }
}
