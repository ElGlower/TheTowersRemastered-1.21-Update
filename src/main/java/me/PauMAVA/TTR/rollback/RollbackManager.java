package me.PauMAVA.TTR.rollback;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor de Regeneración Atómica Integral del Mapa sin reiniciar el servidor.
 * Registra cada bloque original antes de su primera modificación en EventPriority.LOWEST
 * y lo restaura de forma 100% fiel usando BlockData inmutable sin física.
 */
public class RollbackManager implements Listener {

    private final TTRCore plugin;
    // Mapa: Ubicación -> BlockData original antes de cualquier alteración
    private final Map<Location, BlockData> originalBlockData = new ConcurrentHashMap<>();
    private boolean trackingActive = true;

    public RollbackManager(TTRCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startTracking() {
        this.trackingActive = true;
    }

    public void stopTracking() {
        // Mantener el tracking siempre activo para capturar cualquier alteración del mapa
        this.trackingActive = true;
    }

    public boolean isTracking() {
        return trackingActive;
    }

    public int getModifiedBlockCount() {
        return originalBlockData.size();
    }

    public int getRecordedBlockModifications() {
        return originalBlockData.size();
    }

    public void record(Location loc, BlockData data) {
        if (!trackingActive || loc == null || data == null) return;
        originalBlockData.putIfAbsent(loc.clone(), data.clone());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        record(block.getLocation(), block.getBlockData());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // En BlockPlaceEvent, el estado previo intacto (habitualmente AIR) es el que debemos restaurar
        BlockData previousData = event.getBlockReplacedState().getBlockData();
        record(event.getBlock().getLocation(), previousData);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block b : event.blockList()) {
            record(b.getLocation(), b.getBlockData());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block b : event.blockList()) {
            record(b.getLocation(), b.getBlockData());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block b = event.getBlock();
        record(b.getLocation(), b.getBlockData());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        Block b = event.getBlock();
        record(b.getLocation(), b.getBlockData());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();
        if (toBlock.getType() != Material.AIR && !toBlock.isLiquid()) {
            record(toBlock.getLocation(), toBlock.getBlockData());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block b = event.getBlock();
        record(b.getLocation(), b.getBlockData());
    }

    /**
     * Restaura todos los bloques a su estado original exacto y limpia entidades residuales.
     * @return Número de bloques regenerados.
     */
    public int restoreMap() {
        int count = originalBlockData.size();

        if (count > 0) {
            for (Map.Entry<Location, BlockData> entry : originalBlockData.entrySet()) {
                Location loc = entry.getKey();
                BlockData data = entry.getValue();
                if (loc.getWorld() != null) {
                    loc.getBlock().setBlockData(data, false);
                }
            }
            originalBlockData.clear();
        }

        cleanArenaEntities();

        // Teletransportar a los jugadores al lobby de the-towers (Overworld), nunca al nether
        Location lobby = plugin.getConfigManager().getLobbyLocation();
        if (lobby != null && lobby.getWorld() != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(lobby);
            }
        }

        return count;
    }

    public int rollback() {
        return restoreMap();
    }

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
