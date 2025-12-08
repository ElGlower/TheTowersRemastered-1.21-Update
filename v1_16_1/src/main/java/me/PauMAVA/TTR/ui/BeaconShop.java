package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class BeaconShop implements Listener {

    private final String TITLE = ChatColor.DARK_AQUA + "Tienda de Faro";

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        // --- EFECTOS DE EQUIPO (Fila 1) ---
        inv.setItem(10, createItem(Material.IRON_SWORD, "§c§lFuerza I (Equipo)", "§7Duración: 45s", "§eCosto: 1 Esmeralda"));
        inv.setItem(11, createItem(Material.FEATHER, "§b§lVelocidad II (Equipo)", "§7Duración: 45s", "§eCosto: 1 Esmeralda"));
        inv.setItem(12, createItem(Material.GOLDEN_APPLE, "§d§lRegeneración I (Equipo)", "§7Duración: 20s", "§eCosto: 1 Esmeralda"));

        // --- MEJORA ARMADURA (Centro) ---
        inv.setItem(22, createItem(Material.DIAMOND_CHESTPLATE, "§6§lMejorar Armadura Equipo", "§7Sube un nivel de Protección", "§7a todo el equipo.", "§eCosto: 5 Esmeraldas"));

        // --- TIENDA OBJETOS (Abajo) ---
        inv.setItem(29, createItem(Material.GOLDEN_APPLE, "§eManzana Dorada", "§eCosto: 2 Esmeraldas"));
        inv.setItem(30, createItem(Material.ARROW, "§7Pack de Flechas (16)", "§eCosto: 1 Esmeralda"));
        inv.setItem(31, createItem(Material.OBSIDIAN, "§5Obsidiana (4)", "§eCosto: 4 Esmeraldas"));
        inv.setItem(32, createItem(Material.COOKED_BEEF, "§6Filetes (16)", "§eCosto: 1 Esmeralda"));
        inv.setItem(33, createItem(Material.EXPERIENCE_BOTTLE, "§aBotellas de XP (10)", "§eCosto: 2 Esmeraldas"));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();

        int slot = event.getSlot();

        switch (slot) {
            case 10: buyEffect(player, PotionEffectType.STRENGTH, 0, 45); break;
            case 11: buyEffect(player, PotionEffectType.SPEED, 1, 45); break;
            case 12: buyEffect(player, PotionEffectType.REGENERATION, 0, 20); break;

            case 22: upgradeTeamArmor(player); break;

            case 29: buyItem(player, new ItemStack(Material.GOLDEN_APPLE, 1), 2); break;
            case 30: buyItem(player, new ItemStack(Material.ARROW, 16), 1); break;
            case 31: buyItem(player, new ItemStack(Material.OBSIDIAN, 4), 4); break;
            case 32: buyItem(player, new ItemStack(Material.COOKED_BEEF, 16), 1); break;
            case 33: buyItem(player, new ItemStack(Material.EXPERIENCE_BOTTLE, 10), 2); break;
        }
    }

    private void buyEffect(Player player, PotionEffectType type, int amp, int sec) {
        if (pay(player, 1)) {
            TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
            if (team == null) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                TTRTeam pt = TTRCore.getInstance().getTeamHandler().getPlayerTeam(p);
                if (pt != null && pt.getIdentifier().equals(team.getIdentifier())) {
                    p.addPotionEffect(new PotionEffect(type, sec * 20, amp));
                    p.sendMessage("§a¡" + player.getName() + " compró un efecto para el equipo!");
                }
            }
        }
    }

    private void upgradeTeamArmor(Player player) {
        if (!pay(player, 5)) return;

        TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
        if (team == null) return;

        Bukkit.broadcastMessage(team.getIdentifier() + " ha mejorado su armadura!");

        for (Player p : Bukkit.getOnlinePlayers()) {
            TTRTeam pt = TTRCore.getInstance().getTeamHandler().getPlayerTeam(p);
            if (pt != null && pt.getIdentifier().equals(team.getIdentifier())) {
                for (ItemStack armor : p.getInventory().getArmorContents()) {
                    if (armor != null && armor.getType() != Material.AIR) {
                        int current = armor.getEnchantmentLevel(Enchantment.PROTECTION);
                        if (current < 3) {
                            armor.addEnchantment(Enchantment.PROTECTION, current + 1);
                        }
                    }
                }
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
            }
        }
    }

    private void buyItem(Player player, ItemStack item, int cost) {
        if (pay(player, cost)) {
            player.getInventory().addItem(item);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        }
    }

    private boolean pay(Player player, int amount) {
        if (player.getInventory().contains(Material.EMERALD, amount)) {
            removeItem(player, Material.EMERALD, amount);
            return true;
        } else {
            player.sendMessage("§cNo tienes suficientes esmeraldas.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }
    }

    private void removeItem(Player player, Material mat, int amount) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    break;
                } else {
                    amount -= item.getAmount();
                    player.getInventory().remove(item);
                    if (amount == 0) break;
                }
            }
        }
    }
}