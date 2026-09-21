package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gestor de Limpieza de Líquidos y Re-Stock de Cofres de la Arena.
 * Elimina automáticamente cualquier cubo de agua o lava y garantiza
 * que todos los cofres (incluso en chunks no cargados o destruidos) se regeneren al 100%.
 */
public class ChestRestockManager implements Listener {

    private static ChestRestockManager instance;
    private final Map<Location, ItemStack[]> initialChestSnapshots = new HashMap<>();

    public static ChestRestockManager getInstance() {
        if (instance == null) {
            instance = new ChestRestockManager();
        }
        return instance;
    }

    public static boolean isLiquidBucket(Material mat) {
        if (mat == null) return false;
        return mat == Material.WATER_BUCKET ||
               mat == Material.LAVA_BUCKET ||
               mat == Material.AXOLOTL_BUCKET ||
               mat == Material.COD_BUCKET ||
               mat == Material.SALMON_BUCKET ||
               mat == Material.PUFFERFISH_BUCKET ||
               mat == Material.TROPICAL_FISH_BUCKET ||
               mat == Material.TADPOLE_BUCKET;
    }

    /**
     * Purga todos los cubos con líquido de un inventario dado.
     * @return Cantidad de líquidos eliminados.
     */
    public int purgeLiquids(Inventory inv) {
        if (inv == null) return 0;
        int removed = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isLiquidBucket(item.getType())) {
                inv.setItem(i, null);
                removed += item.getAmount();
            }
        }
        return removed;
    }

    /**
     * Guarda el snapshot del inventario de cualquier contenedor si no existía previamente.
     */
    public void snapshotContainer(Container container) {
        if (container == null) return;
        Location loc = container.getLocation();
        if (!initialChestSnapshots.containsKey(loc)) {
            Inventory inv = (container instanceof Chest chest) ? chest.getBlockInventory() : container.getInventory();
            ItemStack[] contents = inv.getContents();
            ItemStack[] copy = new ItemStack[contents.length];
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && !isLiquidBucket(contents[i].getType())) {
                    copy[i] = contents[i].clone();
                } else {
                    copy[i] = null;
                }
            }
            initialChestSnapshots.put(loc, copy);
        }
    }

    public void snapshotChest(Chest chest) {
        snapshotContainer(chest);
    }

    /**
     * Captura el estado original de todos los cofres de la arena escaneando
     * exhaustivamente los chunks de la arena The Towers.
     */
    public void captureInitialChests(World world) {
        if (world == null) return;

        // 1. Escaneo exhaustivo del cuadrante completo de la arena (X: -160 a +160, Z: 928 a 1360)
        for (int cx = -10; cx <= 10; cx++) {
            for (int cz = 58; cz <= 85; cz++) {
                if (!world.isChunkLoaded(cx, cz)) {
                    world.loadChunk(cx, cz, false);
                }
                Chunk chunk = world.getChunkAt(cx, cz);
                if (chunk != null) {
                    for (BlockState state : chunk.getTileEntities()) {
                        if (state instanceof Container container) {
                            snapshotContainer(container);
                        }
                    }
                }
            }
        }

        // 2. Todos los cofres configurados explícitamente en la arena
        if (TTRCore.getInstance().getConfigManager() != null) {
            java.util.List<Location> configured = TTRCore.getInstance().getConfigManager().getAllConfiguredChests();
            for (Location loc : configured) {
                if (loc != null && loc.getWorld() != null) {
                    if (!loc.isChunkLoaded()) loc.getChunk().load();
                    BlockState state = loc.getBlock().getState();
                    if (state instanceof Container container) {
                        snapshotContainer(container);
                    }
                }
            }
        }
    }

    /**
     * Escucha la carga dinámica de chunks para capturar cofres si aún no estaban registrados.
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World arenaWorld = getArenaWorld();
        if (arenaWorld == null || !event.getWorld().equals(arenaWorld)) return;

        TTRMatch match = TTRCore.getInstance().getCurrentMatch();
        if (match != null && match.getStatus() == MatchStatus.LOBBY) {
            for (BlockState state : event.getChunk().getTileEntities()) {
                if (state instanceof Container container) {
                    snapshotContainer(container);
                }
            }
        }
    }

    /**
     * Regenera y limpia todos los cofres de la arena:
     * 1. Carga chunks correspondientes para no perder cofres de torres alejadas.
     * 2. Si el bloque fue destruido o reemplazado por aire, lo vuelve a colocar.
     * 3. Restaura fielmente todos los ítems del snapshot inicial (incluyendo ambas mitades de cofres dobles).
     * 4. Purga cubos de agua y lava y actualiza el estado de bloque (tile entity).
     */
    public int restockAndPurgeArenaChests(boolean restoreFromSnapshot) {
        World arenaWorld = getArenaWorld();
        if (arenaWorld == null) return 0;

        captureInitialChests(arenaWorld);

        int chestsTouched = 0;
        Set<Location> processed = new HashSet<>();

        // 1. Restaurar todos los contenedores de los cuales tenemos snapshot
        for (Map.Entry<Location, ItemStack[]> entry : initialChestSnapshots.entrySet()) {
            Location loc = entry.getKey();
            ItemStack[] snapshot = entry.getValue();
            if (loc == null || loc.getWorld() == null) continue;

            if (!loc.isChunkLoaded()) {
                loc.getChunk().load();
            }

            Block b = loc.getBlock();
            // Si el bloque fue destruido (aire u otro bloque), reconstruirlo como cofre
            if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST && b.getType() != Material.BARREL) {
                b.setType(Material.CHEST, false);
            }

            if (b.getState() instanceof Container container) {
                Inventory inv = (container instanceof Chest chest) ? chest.getBlockInventory() : container.getInventory();
                inv.clear();
                for (int i = 0; i < Math.min(inv.getSize(), snapshot.length); i++) {
                    if (snapshot[i] != null && !isLiquidBucket(snapshot[i].getType())) {
                        inv.setItem(i, snapshot[i].clone());
                    }
                }
                purgeLiquids(inv);
                container.update(true, false);
                processed.add(loc);
                chestsTouched++;
            }
        }

        // 2. Asegurar que los cofres configurados en config.yml también existan y se limpien
        if (TTRCore.getInstance().getConfigManager() != null) {
            java.util.List<Location> configured = TTRCore.getInstance().getConfigManager().getAllConfiguredChests();
            for (Location loc : configured) {
                if (loc == null || loc.getWorld() == null || processed.contains(loc)) continue;

                if (!loc.isChunkLoaded()) loc.getChunk().load();

                Block b = loc.getBlock();
                if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
                    b.setType(Material.CHEST, false);
                }

                if (b.getState() instanceof Container container) {
                    if (restoreFromSnapshot && initialChestSnapshots.containsKey(loc)) {
                        ItemStack[] snapshot = initialChestSnapshots.get(loc);
                        Inventory inv = (container instanceof Chest chest) ? chest.getBlockInventory() : container.getInventory();
                        inv.clear();
                        for (int i = 0; i < Math.min(inv.getSize(), snapshot.length); i++) {
                            if (snapshot[i] != null && !isLiquidBucket(snapshot[i].getType())) {
                                inv.setItem(i, snapshot[i].clone());
                            }
                        }
                    }
                    purgeLiquids((container instanceof Chest chest) ? chest.getBlockInventory() : container.getInventory());
                    container.update(true, false);
                    processed.add(loc);
                    chestsTouched++;
                }
            }
        }

        // 3. Purga adicional de cualquier otro contenedor cargado (barriles, dispensers, etc.)
        for (Chunk chunk : arenaWorld.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (state instanceof Container container && !processed.contains(container.getLocation())) {
                    purgeLiquids(container.getInventory());
                    container.update(true, false);
                    chestsTouched++;
                }
            }
        }

        return chestsTouched;
    }

    public World getArenaWorld() {
        Location lobby = TTRCore.getInstance().getConfigManager().getLobbyLocation();
        if (lobby != null && lobby.getWorld() != null) return lobby.getWorld();
        if (!Bukkit.getWorlds().isEmpty()) return Bukkit.getWorlds().get(0);
        return null;
    }
}
