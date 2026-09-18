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
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de Limpieza de Líquidos y Re-Stock de Cofres de la Arena.
 * Elimina automáticamente cualquier cubo de agua o lava y permite restaurar cofres.
 */
public class ChestRestockManager {

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
     * Captura el estado original de los cofres la primera vez que se cargan.
     */
    public void captureInitialChests(World world) {
        if (world == null) return;
        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (state instanceof Chest chest) {
                    Location loc = chest.getLocation();
                    if (!initialChestSnapshots.containsKey(loc)) {
                        ItemStack[] contents = chest.getBlockInventory().getContents();
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
            }
        }
    }

    /**
     * Barre todos los cofres cargados en el mundo de la arena:
     * 1. Elimina todos los cubos de agua y lava.
     * 2. Si se solicita restock completo, restaura los ítems iniciales.
     */
    public int restockAndPurgeArenaChests(boolean restoreFromSnapshot) {
        World arenaWorld = getArenaWorld();
        if (arenaWorld == null) return 0;

        captureInitialChests(arenaWorld);

        int chestsTouched = 0;
        for (Chunk chunk : arenaWorld.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (state instanceof Container container) {
                    Inventory inv = container.getInventory();
                    purgeLiquids(inv);

                    if (restoreFromSnapshot && state instanceof Chest chest) {
                        ItemStack[] snapshot = initialChestSnapshots.get(chest.getLocation());
                        if (snapshot != null) {
                            chest.getBlockInventory().clear();
                            for (int i = 0; i < snapshot.length; i++) {
                                if (snapshot[i] != null && !isLiquidBucket(snapshot[i].getType())) {
                                    chest.getBlockInventory().setItem(i, snapshot[i].clone());
                                }
                            }
                        }
                    }
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
