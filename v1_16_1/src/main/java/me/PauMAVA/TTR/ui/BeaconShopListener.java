package me.PauMAVA.TTR.ui;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

public class BeaconShopListener implements Listener {

    private final BeaconShop beaconShop = new BeaconShop();

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        String title = event.getView().getTitle();

        if (!title.startsWith(ChatColor.DARK_PURPLE + "Tienda") &&
                !title.startsWith(ChatColor.DARK_BLUE + "TTR") &&
                !title.startsWith(ChatColor.DARK_GREEN + "TTR") &&
                !title.startsWith(ChatColor.GOLD + "TTR")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem.getType() == Material.ARROW && clickedItem.getItemMeta().getDisplayName().contains("Volver")) {
            beaconShop.openMain(player);
            return;
        }

        if (title.equals(BeaconShop.TITLE_MAIN)) {
            if (clickedItem.getType() == Material.IRON_SWORD) beaconShop.openBlocks(player);
            else if (clickedItem.getType() == Material.BREWING_STAND) beaconShop.openUtility(player);
            else if (clickedItem.getType() == Material.BEACON) beaconShop.openUpgrades(player);
            return;
        }

        handleTransaction(player, clickedItem, title);
    }

    private void handleTransaction(Player player, ItemStack item, String title) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;

        List<String> lore = meta.getLore();
        String costLine = null;
        for (String line : lore) {
            if (ChatColor.stripColor(line).startsWith("Costo:")) {
                costLine = ChatColor.stripColor(line);
                break;
            }
        }
        if (costLine == null) return;

        String[] parts = costLine.split(" ");
        int price = Integer.parseInt(parts[1]);
        String currencyName = parts[2];
        Material currencyMat = currencyName.equalsIgnoreCase("Esmeraldas") ? Material.EMERALD : Material.COAL;

        if (title.equals(BeaconShop.TITLE_UPGRADES)) {
            if (canBuyUpgrade(player, item, price, currencyMat)) {
                removeItem(player, currencyMat, price);
                applyTeamUpgrade(player, item);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0.5f);
            }
        } else {
            if (player.getInventory().contains(currencyMat, price)) {
                removeItem(player, currencyMat, price);

                ItemStack toGive = new ItemStack(item.getType(), item.getAmount());

                if (item.getItemMeta().hasEnchants() || item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION) {
                    toGive.setItemMeta(item.getItemMeta());
                }

                ItemMeta giveMeta = toGive.getItemMeta();
                giveMeta.setLore(null);
                toGive.setItemMeta(giveMeta);

                // Aplicar nivel actual a la armadura nueva comprada
                TTRTeam team = me.PauMAVA.TTR.TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
                if (team != null && team.getArmorProtectionLevel() > 0) {
                    String type = toGive.getType().toString();
                    if (type.contains("HELMET") || type.contains("CHESTPLATE") || type.contains("LEGGINGS") || type.contains("BOOTS")) {
                        giveMeta = toGive.getItemMeta();
                        giveMeta.addEnchant(Enchantment.PROTECTION, team.getArmorProtectionLevel(), true);
                        toGive.setItemMeta(giveMeta);
                    }
                }

                player.getInventory().addItem(toGive);
                player.sendMessage(ChatColor.GREEN + "Comprado: " + meta.getDisplayName());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            } else {
                player.sendMessage(ChatColor.RED + "Te faltan materiales: " + currencyName);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            }
        }
    }

    private boolean canBuyUpgrade(Player player, ItemStack item, int price, Material currency) {
        TTRTeam team = me.PauMAVA.TTR.TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
        if (team == null) return false;

        if (!player.getInventory().contains(currency, price)) {
            player.sendMessage(ChatColor.RED + "Te faltan materiales.");
            return false;
        }

        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (item.getType().toString().contains("CHESTPLATE")) {
            int requestedLevel = 1;
            if (itemName.contains("IV")) requestedLevel = 4;
            else if (itemName.contains("III")) requestedLevel = 3;
            else if (itemName.contains("II")) requestedLevel = 2;

            int currentLevel = team.getArmorProtectionLevel();

            if (currentLevel >= requestedLevel) {
                player.sendMessage(ChatColor.RED + "¡Tu equipo ya tiene nivel " + currentLevel + "!");
                return false;
            }
            if (requestedLevel > currentLevel + 1) {
                player.sendMessage(ChatColor.RED + "Debes comprar el nivel anterior primero.");
                return false;
            }
        } else if (item.getType() == Material.SUGAR && team.hasTeamSpeed()) {
            player.sendMessage(ChatColor.RED + "¡Ya tienes Velocidad!");
            return false;
        } else if (item.getType() == Material.GOLDEN_PICKAXE && team.hasTeamHaste()) {
            player.sendMessage(ChatColor.RED + "¡Ya tienes Prisa!");
            return false;
        }

        return true;
    }

    private void applyTeamUpgrade(Player buyer, ItemStack item) {
        TTRTeam team = me.PauMAVA.TTR.TTRCore.getInstance().getTeamHandler().getPlayerTeam(buyer);
        if (team == null) return;

        String upgradeName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Material mat = item.getType();

        // 1. Guardar en la base de datos del equipo
        if (mat.toString().contains("CHESTPLATE")) {
            int level = 1;
            if (upgradeName.contains("IV")) level = 4;
            else if (upgradeName.contains("III")) level = 3;
            else if (upgradeName.contains("II")) level = 2;
            team.setArmorProtectionLevel(level);
        } else if (mat == Material.SUGAR) team.setTeamSpeed(true);
        else if (mat == Material.GOLDEN_PICKAXE) team.setTeamHaste(true);

        // 2. APLICAR A TODOS (CORREGIDO: Usando TTRTeam.getPlayers())
        // Antes usábamos scoreboard vanilla, lo cual fallaba si el plugin usaba su propio sistema.
        for (UUID uuid : team.getPlayers()) {
            Player teammate = Bukkit.getPlayer(uuid);
            if (teammate != null && teammate.isOnline()) {
                teammate.sendMessage(ChatColor.GOLD + "¡" + buyer.getName() + " ha mejorado el equipo a " + upgradeName + "!");
                teammate.playSound(teammate.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0.5f);

                // Forzar actualización inmediata
                updatePlayerEffects(teammate, team);
            }
        }
    }

    private void updatePlayerEffects(Player p, TTRTeam team) {
        // Pociones
        if (team.hasTeamSpeed())
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        if (team.hasTeamHaste())
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false));

        // Armadura Puesta
        int protLevel = team.getArmorProtectionLevel();
        if (protLevel > 0) {
            ItemStack[] armor = p.getInventory().getArmorContents();
            boolean changed = false;

            for (int i = 0; i < armor.length; i++) {
                ItemStack piece = armor[i];
                if (piece != null && piece.getType() != Material.AIR) {
                    // Validar que sea una pieza de armadura
                    String type = piece.getType().toString();
                    if (type.contains("HELMET") || type.contains("CHESTPLATE") || type.contains("LEGGINGS") || type.contains("BOOTS")) {

                        ItemMeta meta = piece.getItemMeta();
                        if (meta != null) {
                            // true = permite sobrescribir o ignorar restricciones
                            meta.addEnchant(Enchantment.PROTECTION, protLevel, true);
                            piece.setItemMeta(meta);
                            armor[i] = piece; // Actualizamos el array
                            changed = true;
                        }
                    }
                }
            }
            if (changed) {
                p.getInventory().setArmorContents(armor); // Inyectamos la armadura modificada
            }
        }
        p.updateInventory(); // Obligatorio para refrescar el cliente
    }

    private void removeItem(Player player, Material mat, int amount) {
        int remaining = amount;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.getType() == mat) {
                if (is.getAmount() > remaining) {
                    is.setAmount(is.getAmount() - remaining);
                    remaining = 0;
                    break;
                } else {
                    remaining -= is.getAmount();
                    is.setAmount(0);
                }
            }
        }
        player.updateInventory();
    }
}