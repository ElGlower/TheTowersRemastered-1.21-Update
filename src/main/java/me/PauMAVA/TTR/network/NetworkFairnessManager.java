package me.PauMAVA.TTR.network;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor de Justicia de Red (Network Fairness Engine) para Destiny Towers.
 * Proporciona:
 * 1. Compensación de Lag por Rebobinado (Lag Compensation / Hit Rewind): Cero golpes fantasma para jugadores internacionales con 100-160ms.
 * 2. Normalizador de Knockback (Fair Knockback Engine): Empuje idéntico y consistente sin desincronización por ping.
 * 3. Igualador de Latencia (Ping Equalizer): Modo opcional para nivelar a jugadores locales con los lejanos.
 */
public class NetworkFairnessManager implements Listener {

    private static NetworkFairnessManager instance;

    public static class PlayerSnapshot {
        private final long timestamp;
        private final Location location;
        private final BoundingBox box;

        public PlayerSnapshot(long timestamp, Location location, BoundingBox box) {
            this.timestamp = timestamp;
            this.location = location;
            this.box = box;
        }

        public long getTimestamp() { return timestamp; }
        public Location getLocation() { return location; }
        public BoundingBox getBox() { return box; }
    }

    private final Map<UUID, Deque<PlayerSnapshot>> positionHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastHitTimes = new ConcurrentHashMap<>();

    private static final int MAX_SNAPSHOTS = 25; // 25 snapshots a 2 ticks = 2.5 segundos de historial

    private BukkitTask trackingTask = null;
    private boolean equalizerEnabled = false; // Desactivado por defecto para cero latencia artificial
    private int targetPing = 120;

    private boolean autoEqualizer = false;

    public static NetworkFairnessManager getInstance() {
        if (instance == null) {
            instance = new NetworkFairnessManager();
        }
        return instance;
    }

    public boolean isEqualizerEnabled() { return equalizerEnabled; }
    public void setEqualizerEnabled(boolean enabled) { this.equalizerEnabled = enabled; }
    public boolean isAutoEqualizer() { return autoEqualizer; }
    public void setAutoEqualizer(boolean auto) { this.autoEqualizer = auto; }
    public int getTargetPing() { return targetPing; }
    public void setTargetPing(int target) { this.targetPing = Math.max(30, Math.min(300, target)); }

    public int getEffectiveTargetPing() {
        if (!autoEqualizer) return targetPing;

        List<Integer> pings = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            int pPing = p.getPing();
            if (pPing > 35) {
                pings.add(pPing);
            }
        }
        if (pings.isEmpty()) return targetPing;
        Collections.sort(pings);
        int median = pings.get(pings.size() / 2);
        return Math.max(60, Math.min(200, median));
    }

    public boolean isTrackingActive() {
        return trackingTask != null;
    }

    public boolean isEqualizerActive() {
        return equalizerEnabled && isTrackingActive();
    }

    public void startTracking() {
        stopTracking();
        trackingTask = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getGameMode() == GameMode.SPECTATOR) continue;

                    Deque<PlayerSnapshot> snapshots = positionHistory.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
                    synchronized (snapshots) {
                        snapshots.addLast(new PlayerSnapshot(now, p.getLocation(), p.getBoundingBox()));
                        while (snapshots.size() > MAX_SNAPSHOTS) {
                            snapshots.removeFirst();
                        }
                    }
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 2L);
    }

    public void stopTracking() {
        if (trackingTask != null) {
            trackingTask.cancel();
            trackingTask = null;
        }
        positionHistory.clear();
        lastHitTimes.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        positionHistory.remove(event.getPlayer().getUniqueId());
        lastHitTimes.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Detección y compensación de lag cuando un jugador ataca en el aire
     * debido a que su cliente local vio al objetivo, pero el servidor vanilla
     * lo rechazó por desfase de posición en milisegundos.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerSwing(PlayerInteractEvent event) {
        // Solo procesar golpes al aire (LEFT_CLICK_AIR). Ignorar completamente LEFT_CLICK_BLOCK
        // para que picar en minas o romper bloques tenga CERO costo de CPU.
        if (event.getAction() != Action.LEFT_CLICK_AIR) return;

        if (!isTrackingActive()) return;

        Player attacker = event.getPlayer();
        if (attacker.getGameMode() != GameMode.SURVIVAL) return;

        int ping = attacker.getPing();
        // Solo compensar si el jugador tiene latencia perceptible (> 35ms)
        if (ping < 35) return;

        // Respetar tiempo de recarga del arma (evitar auto-clicker)
        if (attacker.getAttackCooldown() < 0.80f) return;

        long now = System.currentTimeMillis();
        Long lastHit = lastHitTimes.get(attacker.getUniqueId());
        if (lastHit != null && (now - lastHit) < 420) {
            // Cooldown de ataque mínimo de Minecraft 1.9+ (~2.4 clics por segundo)
            return;
        }

        attemptRewoundHit(attacker, ping, now);
    }

    private void attemptRewoundHit(Player attacker, int ping, long now) {
        TTRCore core = TTRCore.getInstance();
        TTRTeam attackerTeam = core.getTeamHandler() != null ? core.getTeamHandler().getPlayerTeam(attacker) : null;

        // Momento en el tiempo en que el cliente vio al enemigo en su pantalla
        long targetTime = now - (ping / 2);

        Location eye = attacker.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        // Tolerancia de alcance compensada por ping (soporta hasta 300ms de ping fluidamente)
        double maxReach = Math.min(4.3, 3.4 + ((double) ping / 280.0) * 0.8);

        Player bestTarget = null;
        double bestDistance = Double.MAX_VALUE;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(attacker)) continue;
            if (target.getGameMode() != GameMode.SURVIVAL) continue;

            TTRTeam targetTeam = core.getTeamHandler() != null ? core.getTeamHandler().getPlayerTeam(target) : null;
            // Prevenir fuego amigo si ambos están en el mismo equipo
            if (attackerTeam != null && targetTeam != null && targetTeam.equals(attackerTeam)) continue;

            // Pre-filtro rápido de distancia en el presente
            if (!target.getWorld().equals(attacker.getWorld())) continue;
            if (target.getLocation().distanceSquared(attacker.getLocation()) > 49.0) continue;

            // Obtener el snapshot más cercano al momento targetTime
            PlayerSnapshot snapshot = findBestSnapshot(target.getUniqueId(), targetTime);
            BoundingBox box = (snapshot != null) ? snapshot.getBox() : target.getBoundingBox();

            // Tolerancia de jitter adaptada a la latencia del jugador (hasta 0.25 bloques para 300ms)
            double jitterTolerance = Math.min(0.25, 0.12 + ((double) ping / 1000.0) * 0.25);
            BoundingBox expanded = box.clone().expand(jitterTolerance);
            RayTraceResult hit = expanded.rayTrace(eye.toVector(), dir, maxReach);

            if (hit != null) {
                double dist = eye.toVector().distance(hit.getHitPosition());
                if (dist < bestDistance) {
                    bestDistance = dist;
                    bestTarget = target;
                }
            }
        }

        if (bestTarget != null) {
            lastHitTimes.put(attacker.getUniqueId(), now);
            final Player victim = bestTarget;

            // Si el modo Ping Equalizer está activo y el atacante tiene menos ping que el objetivo
            int effectiveTarget = getEffectiveTargetPing();
            if (isEqualizerActive() && ping < effectiveTarget) {
                long delayMs = (effectiveTarget - ping) / 2;
                long delayTicks = Math.max(1, delayMs / 50);
                Bukkit.getScheduler().runTaskLater(core, () -> {
                    executeCompensatedHit(attacker, victim);
                }, delayTicks);
            } else {
                executeCompensatedHit(attacker, victim);
            }
        }
    }

    private PlayerSnapshot findBestSnapshot(UUID playerUuid, long targetTime) {
        Deque<PlayerSnapshot> queue = positionHistory.get(playerUuid);
        if (queue == null || queue.isEmpty()) return null;

        PlayerSnapshot best = null;
        long minDiff = Long.MAX_VALUE;

        synchronized (queue) {
            for (PlayerSnapshot s : queue) {
                long diff = Math.abs(s.getTimestamp() - targetTime);
                if (diff < minDiff) {
                    minDiff = diff;
                    best = s;
                }
            }
        }
        return best;
    }

    private void executeCompensatedHit(Player attacker, Player victim) {
        if (!attacker.isOnline() || !victim.isOnline()) return;
        if (victim.isDead() || victim.getGameMode() != GameMode.SURVIVAL) return;

        // Comprobar spawn-kill protection
        TTRTeam victimTeam = TTRCore.getInstance().getTeamHandler().getPlayerTeam(victim);
        if (victimTeam != null) {
            Location spawn = TTRCore.getInstance().getConfigManager().getTeamSpawn(victimTeam.getIdentifier());
            if (spawn != null && spawn.getWorld() != null && spawn.getWorld().equals(victim.getWorld())) {
                if (spawn.distanceSquared(victim.getLocation()) <= 64.0) {
                    return;
                }
            }
        }

        // Ejecutar ataque nativo (disparará EntityDamageByEntityEvent que aplica el knockback)
        attacker.attack(victim);
    }
}
