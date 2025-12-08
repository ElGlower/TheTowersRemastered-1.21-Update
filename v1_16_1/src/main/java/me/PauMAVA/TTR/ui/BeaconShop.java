package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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

    private final String TITLE = ChatColor.DARK_AQUA + "Mejoras de Equipo (1 Esmeralda)";

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        inv.setItem(11, createItem(Material.IRON_SWORD, ChatColor.RED + "Fuerza I (Equipo)", "§7Duración: 45s", "§eCosto: 1 Esmeralda"));
        inv.setItem(13, createItem(Material.FEATHER, ChatColor.AQUA + "Velocidad II (Equipo)", "§7Duración: 45s", "§eCosto: 1 Esmeralda"));
        inv.setItem(15, createItem(Material.GOLDEN_APPLE, ChatColor.LIGHT_PURPLE + "Regeneración I (Equipo)", "§7Duración: 20s", "§eCosto: 1 Esmeralda"));

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

        if (!player.getInventory().contains(Material.EMERALD, 1)) {
            player.sendMessage(ChatColor.RED + "¡Necesitas una Esmeralda para activar el Faro!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            player.closeInventory();
            return;
        }

        TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
        if (team == null) return;

        PotionEffect effect = null;
        String effectName = "";

        switch (event.getSlot()) {
            case 11:
                effect = new PotionEffect(PotionEffectType.STRENGTH, 45 * 20, 0);
                effectName = "Fuerza";
                break;
            case 13:
                effect = new PotionEffect(PotionEffectType.SPEED, 45 * 20, 1);
                effectName = "Velocidad";
                break;
            case 15:
                effect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 0);
                effectName = "Regeneración";
                break;
        }

        if (effect != null) {
            removeItem(player, Material.EMERALD, 1);
            for (Player p : Bukkit.getOnlinePlayers()) {
                TTRTeam pTeam = TTRCore.getInstance().getTeamHandler().getPlayerTeam(p);
                if (pTeam != null && pTeam.getIdentifier().equals(team.getIdentifier())) {
                    p.addPotionEffect(effect);
                    p.sendMessage(ChatColor.GREEN + "¡" + player.getName() + " ha activado " + ChatColor.GOLD + effectName + ChatColor.GREEN + " para el equipo!");
                    p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 10, 1);
                }
            }
            player.closeInventory();
        }
    }

    private void removeItem(Player player, Material mat, int amount) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                } else {
                    player.getInventory().remove(item);
                }
                break;
            }
        }
    }
}