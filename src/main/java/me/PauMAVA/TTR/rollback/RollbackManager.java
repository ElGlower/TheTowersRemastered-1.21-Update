package me.PauMAVA.TTR.rollback;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor de Regeneración Automática del Mapa sin reiniciar el servidor.
 * Registra cada bloque alterado durante la partida y lo restaura de forma atómica.
 */
public class RollbackManager implements Listener {

    private final TTRCore plugin;
    // Mapa de bloques: Ubicación -> Estado Original (Capturado antes de la primera modificación)
    private final Map<Location, BlockState> originalBlockStates = new ConcurrentHashMap<>();
    private boolean trackingActive = false;

    public RollbackManager(TTRCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startTracking() {
        this.originalBlockStates.clear();
        this.trackingActive = true;
    }

    public void stopTracking() {
        this.trackingActive = false;
    }

    public boolean isTracking() {
        return trackingActive;
    }

    public int getModifiedBlockCount() {
        return originalBlockStates.size();
    }

    public int getRecordedBlockModifications() {
        return getModifiedBlockCount();
    }

    /**
     * Guarda el estado original del bloque si no ha sido registrado previamente.
     */
    private void recordOriginalState(Block block) {
        if (!trackingActive) return;
        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) return;

        Location loc = block.getLocation();
        originalBlockStates.putIfAbsent(loc, block.getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        recordOriginalState(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        recordOriginalState(event.getBlockReplacedState().getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            recordOriginalState(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            recordOriginalState(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        recordOriginalState(event.getBlock());
    }

    /**
     * Restaura todos los bloques a su estado inicial y limpia entidades residuales (ítems, proyectiles).
     * @return El número de bloques restaurados.
     */
    public int restoreMap() {
        int restoredCount = originalBlockStates.size();

        if (restoredCount > 0) {
            // Restaurar bloques sin física para evitar lag y cascadas de redstone/agua
            for (Map.Entry<Location, BlockState> entry : originalBlockStates.entrySet()) {
                BlockState originalState = entry.getValue();
                originalState.update(true, false);
            }
            originalBlockStates.clear();
        }

        // Limpieza de entidades residuales de la arena
        cleanArenaEntities();

        return restoredCount;
    }

    public int rollback() {
        return restoreMap();
    }

    /**
     * Elimina ítems tirados, orbes de experiencia y proyectiles huérfanos.
     */
    public void cleanArenaEntities() {
        Location lobby = plugin.getConfigManager().getLobbyLocation();
        World arenaWorld = (lobby != null) ? lobby.getWorld() : (Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0));

        if (arenaWorld != null) {
            for (Entity entity : arenaWorld.getEntities()) {
                if (entity instanceof Item || entity instanceof ExperienceOrb ||
                    entity instanceof Arrow || entity instanceof Fireball ||
                    entity instanceof TNTPrimed || entity instanceof WindCharge) {
                    entity.remove();
                }
            }
        }
    }
}
