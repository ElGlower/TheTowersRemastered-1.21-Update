package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class BeaconShopListener implements Listener {

    private final BeaconShop beaconShop = new BeaconShop();

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        String title = event.getView().getTitle();

        if (!title.equals(BeaconShop.TITLE_MAIN) &&
            !title.equals(BeaconShop.TITLE_BLOCKS) &&
            !title.equals(BeaconShop.TITLE_UTILITY) &&
            !title.equals(BeaconShop.TITLE_UPGRADES)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        if (clickedItem.getType() == Material.ARROW && meta.hasDisplayName() && meta.getDisplayName().contains(TextUtil.toTiny("Volver"))) {
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
        if (meta == null) return;

        int price = 0;
        Material currencyMat = Material.EMERALD;
        String upgradeKey = null;

        if (meta.getPersistentDataContainer().has(BeaconShop.KEY_PRICE, PersistentDataType.INTEGER)) {
            price = meta.getPersistentDataContainer().get(BeaconShop.KEY_PRICE, PersistentDataType.INTEGER);
            String currName = meta.getPersistentDataContainer().get(BeaconShop.KEY_CURRENCY, PersistentDataType.STRING);
            if (currName != null) currencyMat = Material.valueOf(currName);
            if (meta.getPersistentDataContainer().has(BeaconShop.KEY_UPGRADE, PersistentDataType.STRING)) {
                upgradeKey = meta.getPersistentDataContainer().get(BeaconShop.KEY_UPGRADE, PersistentDataType.STRING);
            }
        }

        if (price <= 0) return;

        String currencyName = (currencyMat == Material.EMERALD) ? "Esmeraldas" : "Carbón";

        if (title.equals(BeaconShop.TITLE_UPGRADES) && upgradeKey != null) {
            if (canBuyUpgrade(player, upgradeKey, price, currencyMat)) {
                removeItem(player, currencyMat, price);
                applyTeamUpgrade(player, upgradeKey);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
            }
        } else {
            if (player.getInventory().contains(currencyMat, price)) {
                removeItem(player, currencyMat, price);

                ItemStack toGive = new ItemStack(item.getType(), item.getAmount());
                ItemMeta giveMeta = item.getItemMeta().clone();
                giveMeta.setLore(null);
                toGive.setItemMeta(giveMeta);

                // Aplicar nivel de protección del equipo a armaduras compradas
                TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
                if (team != null && team.getArmorProtectionLevel() > 0) {
                    String type = toGive.getType().toString();
                    if (type.contains("HELMET") || type.contains("CHESTPLATE") || type.contains("LEGGINGS") || type.contains("BOOTS")) {
                        giveMeta = toGive.getItemMeta();
                        giveMeta.addEnchant(Enchantment.PROTECTION, team.getArmorProtectionLevel(), true);
                        toGive.setItemMeta(giveMeta);
                    }
                }

                player.getInventory().addItem(toGive);
                player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Comprado: ") + meta.getDisplayName());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            } else {
                player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Te faltan materiales: ") + 
                        ChatColor.YELLOW + TextUtil.toTiny(price + " " + currencyName));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }

    private boolean canBuyUpgrade(Player player, String upgradeKey, int price, Material currency) {
        TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
        if (team == null) return false;

        if (!player.getInventory().contains(currency, price)) {
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Te faltan materiales."));
            return false;
        }

        if (upgradeKey.startsWith("prot_")) {
            int requestedLevel = Integer.parseInt(upgradeKey.replace("prot_", ""));
            int currentLevel = team.getArmorProtectionLevel();

            if (currentLevel >= requestedLevel) {
                player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Tu equipo ya tiene nivel " + currentLevel + "!"));
                return false;
            }
            if (requestedLevel > currentLevel + 1) {
                player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Debes comprar el nivel anterior primero."));
                return false;
            }
        } else if (upgradeKey.equals("speed") && team.hasTeamSpeed()) {
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Tu equipo ya tiene Velocidad!"));
            return false;
        } else if (upgradeKey.equals("haste") && team.hasTeamHaste()) {
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Tu equipo ya tiene Prisa!"));
            return false;
        }

        return true;
    }

    private void applyTeamUpgrade(Player buyer, String upgradeKey) {
        TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(buyer);
        if (team == null) return;

        String upgradeDisplayName = upgradeKey;
        if (upgradeKey.startsWith("prot_")) {
            int level = Integer.parseInt(upgradeKey.replace("prot_", ""));
            team.setArmorProtectionLevel(level);
            upgradeDisplayName = "Protección " + level;
        } else if (upgradeKey.equals("speed")) {
            team.setTeamSpeed(true);
            upgradeDisplayName = "Velocidad I";
        } else if (upgradeKey.equals("haste")) {
            team.setTeamHaste(true);
            upgradeDisplayName = "Prisa Minera I";
        }

        for (UUID uuid : team.getPlayers()) {
            Player teammate = Bukkit.getPlayer(uuid);
            if (teammate != null && teammate.isOnline()) {
                teammate.sendMessage(TTRPrefix.TTR_GAME + ChatColor.GOLD + buyer.getName() + 
                        ChatColor.YELLOW + TextUtil.toTiny(" ha mejorado el equipo a ") + 
                        ChatColor.GREEN + TextUtil.toTiny(upgradeDisplayName) + "!");
                teammate.playSound(teammate.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
                updatePlayerEffects(teammate, team);
            }
        }
    }

    private void updatePlayerEffects(Player p, TTRTeam team) {
        if (team.hasTeamSpeed()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        }
        if (team.hasTeamHaste()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false));
        }

        int protLevel = team.getArmorProtectionLevel();
        if (protLevel > 0) {
            ItemStack[] armor = p.getInventory().getArmorContents();
            boolean changed = false;

            for (int i = 0; i < armor.length; i++) {
                ItemStack piece = armor[i];
                if (piece != null && piece.getType() != Material.AIR) {
                    String type = piece.getType().toString();
                    if (type.contains("HELMET") || type.contains("CHESTPLATE") || type.contains("LEGGINGS") || type.contains("BOOTS")) {
                        ItemMeta meta = piece.getItemMeta();
                        if (meta != null) {
                            meta.addEnchant(Enchantment.PROTECTION, protLevel, true);
                            piece.setItemMeta(meta);
                            armor[i] = piece;
                            changed = true;
                        }
                    }
                }
            }
            if (changed) {
                p.getInventory().setArmorContents(armor);
            }
        }
        p.updateInventory();
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
