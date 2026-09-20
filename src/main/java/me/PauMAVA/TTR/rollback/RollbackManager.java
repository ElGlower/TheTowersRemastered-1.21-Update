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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import org.bukkit.inventory.ItemStack;
import me.PauMAVA.TTR.match.ChestRestockManager;
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
    // Mapa: Ubicación -> Inventario original antes de cualquier saqueo o rotura
    private final Map<Location, ItemStack[]> originalChestContents = new ConcurrentHashMap<>();
    private boolean trackingActive = true;
    private boolean editMode = false;

    public RollbackManager(TTRCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startTracking() {
        if (!editMode) {
            this.trackingActive = true;
        }
    }

    public void stopTracking() {
        if (!editMode) {
            this.trackingActive = true;
        }
    }

    public boolean isTracking() {
        return trackingActive && !editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        this.trackingActive = !editMode;
    }

    public void clearHistory() {
        this.originalBlockData.clear();
        this.originalChestContents.clear();
    }

    public int getModifiedBlockCount() {
        return originalBlockData.size();
    }

    public int getRecordedBlockModifications() {
        return originalBlockData.size();
    }

    public void record(Location loc, BlockData data) {
        if (editMode || !trackingActive || loc == null || data == null) return;
        originalBlockData.putIfAbsent(loc.clone(), data.clone());
    }

    public void recordContainer(Location loc, org.bukkit.inventory.Inventory inv) {
        if (editMode || !trackingActive || loc == null || inv == null) return;
        if (!originalChestContents.containsKey(loc)) {
            org.bukkit.inventory.ItemStack[] items = inv.getContents();
            org.bukkit.inventory.ItemStack[] copy = new org.bukkit.inventory.ItemStack[items.length];
            for (int i = 0; i < items.length; i++) {
                copy[i] = (items[i] != null) ? items[i].clone() : null;
            }
            originalChestContents.put(loc.clone(), copy);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        record(block.getLocation(), block.getBlockData());
        if (block.getState() instanceof org.bukkit.block.Container container) {
            recordContainer(block.getLocation(), container.getInventory());
        }
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
            if (b.getState() instanceof org.bukkit.block.Container container) {
                recordContainer(b.getLocation(), container.getInventory());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block b : event.blockList()) {
            record(b.getLocation(), b.getBlockData());
            if (b.getState() instanceof org.bukkit.block.Container container) {
                recordContainer(b.getLocation(), container.getInventory());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block b = event.getClickedBlock();
            if (b.getState() instanceof org.bukkit.block.Container container) {
                record(b.getLocation(), b.getBlockData());
                recordContainer(b.getLocation(), container.getInventory());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof org.bukkit.block.Container container) {
            Location loc = container.getLocation();
            if (loc != null) {
                Block b = loc.getBlock();
                record(loc, b.getBlockData());
                recordContainer(loc, container.getInventory());
            }
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
        record(toBlock.getLocation(), toBlock.getBlockData());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block b = event.getBlockClicked().getRelative(event.getBlockFace());
        record(b.getLocation(), b.getBlockData());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block b = event.getBlockClicked();
        record(b.getLocation(), b.getBlockData());
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

        // 2. Restaurar contenido íntegro de todos los cofres modificados o saqueados
        if (!originalChestContents.isEmpty()) {
            for (Map.Entry<Location, org.bukkit.inventory.ItemStack[]> entry : originalChestContents.entrySet()) {
                Location loc = entry.getKey();
                org.bukkit.inventory.ItemStack[] contents = entry.getValue();
                if (loc.getWorld() != null) {
                    Block b = loc.getBlock();
                    if (b.getState() instanceof org.bukkit.block.Container container) {
                        container.getInventory().clear();
                        for (int i = 0; i < contents.length; i++) {
                            if (contents[i] != null && !ChestRestockManager.isLiquidBucket(contents[i].getType())) {
                                container.getInventory().setItem(i, contents[i].clone());
                            }
                        }
                    }
                }
            }
            originalChestContents.clear();
        }

        // 3. Reabastecer y purgar cubos de agua/lava de todos los cofres de la arena
        ChestRestockManager.getInstance().restockAndPurgeArenaChests(true);

        cleanArenaEntities();

        // Solo teletransportar si los jugadores están fuera del mundo del lobby (ej. al resetear mapa en partida)
        Location lobby = plugin.getConfigManager().getLobbyLocation();
        if (lobby != null && lobby.getWorld() != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getWorld().equals(lobby.getWorld()) || p.getLocation().distanceSquared(lobby) > 100.0) {
                    p.teleport(lobby);
                }
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
