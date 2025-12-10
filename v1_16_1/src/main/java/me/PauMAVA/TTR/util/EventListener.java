package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.BeaconShop;
import me.PauMAVA.TTR.ui.TeamSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EventListener implements Listener {

    private final TTRCore plugin;

    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!plugin.enabled()) return;
        String title = event.getView().getTitle();

        if (!title.equals(BeaconShop.TITLE_MAIN) &&
                !title.equals(BeaconShop.TITLE_BLOCKS) &&
                !title.equals(BeaconShop.TITLE_UTILITY) &&
                !title.equals(BeaconShop.TITLE_UPGRADES)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        Player p = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String cleanName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        BeaconShop shop = new BeaconShop();

        if (title.equals(BeaconShop.TITLE_MAIN)) {
            if (cleanName.contains("Bloques")) shop.openBlocks(p);
            else if (cleanName.contains("Utilidad")) shop.openUtility(p);
            else if (cleanName.contains("Mejoras")) shop.openUpgrades(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        if (clicked.getType() == Material.ARROW && cleanName.contains("Volver")) {
            shop.openMain(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        int price = getPriceFromLore(clicked);
        Material currency = getCurrencyFromLore(clicked);

        if (price == -1 || currency == null) return;

        if (!p.getInventory().contains(currency, price)) {
            String currencyName = (currency == Material.EMERALD) ? "esmeraldas" : "carbón";
            p.sendMessage(ChatColor.RED + "¡No tienes suficientes " + currencyName + "!");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        boolean success = false;
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);

        if (cleanName.contains("Team Prot")) {
            if (team == null) { p.sendMessage(ChatColor.RED + "Necesitas equipo."); return; }
            int req = 1;
            if (cleanName.contains("II")) req = 2;
            if (cleanName.contains("III")) req = 3;
            if (cleanName.contains("IV")) req = 4;

            int currentLevel = team.getArmorProtectionLevel();

            if (currentLevel >= req) {
                p.sendMessage(ChatColor.RED + "¡Tu equipo ya tiene este nivel!"); return;
            }
            if (currentLevel < req - 1) {
                p.sendMessage(ChatColor.RED + "¡Compra el nivel anterior primero!"); return;
            }

            team.setArmorProtectionLevel(req);
            applyTeamArmorUpgrade(team, req);
            broadcastTeamMessage(team, ChatColor.GREEN + p.getName() + " compró Protección " + req + "!");
            success = true;

        } else if (cleanName.contains("Team Speed")) {
            if (team != null && !team.hasTeamSpeed()) {
                team.setTeamSpeed(true);
                applyTeamEffect(team, PotionEffectType.SPEED);
                broadcastTeamMessage(team, ChatColor.AQUA + p.getName() + " compró Velocidad!");
                success = true;
            }
        } else if (cleanName.contains("Team Haste")) {
            if (team != null && !team.hasTeamHaste()) {
                team.setTeamHaste(true);
                applyTeamEffect(team, PotionEffectType.HASTE);
                broadcastTeamMessage(team, ChatColor.GOLD + p.getName() + " compró Prisa!");
                success = true;
            }
        }
        else {
            Material type = clicked.getType();

            if (type == Material.SHIELD) {
                ItemStack shield = new ItemStack(Material.SHIELD);
                Damageable meta = (Damageable) shield.getItemMeta();
                meta.setDamage(300);
                meta.setDisplayName(ChatColor.YELLOW + "Escudo Frágil");
                shield.setItemMeta(meta);
                p.getInventory().addItem(shield);
            }
            else if (type == Material.FIRE_CHARGE) {
                p.getInventory().addItem(new ItemStack(Material.FIRE_CHARGE));
            }

            else {
                ItemStack toGive = new ItemStack(type);
                if (type == Material.WHITE_WOOL) toGive.setAmount(32);
                else if (type == Material.OAK_PLANKS) toGive.setAmount(16);
                else if (type == Material.END_STONE) toGive.setAmount(12);
                else if (type == Material.OBSIDIAN) toGive.setAmount(4);
                else if (type == Material.ARROW) toGive.setAmount(8);
                else if (type == Material.BREAD) toGive.setAmount(4);
                else if (type == Material.WIND_CHARGE) toGive.setAmount(2);
                else if (type == Material.POTION || type == Material.SPLASH_POTION) {
                    toGive = getPotionItem(cleanName, type);
                } else toGive.setAmount(1);

                p.getInventory().addItem(toGive);
            }
            success = true;
        }

        if (success) {
            removeItems(p, currency, price);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            if (!cleanName.contains("Team")) p.sendMessage(ChatColor.GREEN + "¡Comprado!");
        }
    }

    private ItemStack getPotionItem(String name, Material mat) {
        ItemStack potion = new ItemStack(mat);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (name.contains("Salto")) meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 900, 1), true);
        else if (name.contains("Regeneración")) meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1), true);
        else if (name.contains("Daño")) meta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + name);
        potion.setItemMeta(meta);
        return potion;
    }

    private void applyTeamArmorUpgrade(TTRTeam team, int level) {
        for (UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                ItemStack[] armor = p.getInventory().getArmorContents();
                for (ItemStack piece : armor) {
                    if (piece != null && piece.getType() != Material.AIR) {
                        if (piece.getType().toString().contains("LEATHER_")) {
                            piece.addEnchantment(Enchantment.PROTECTION, level);
                        }
                    }
                }
                p.getInventory().setArmorContents(armor);
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
            }
        }
    }

    private void applyTeamEffect(TTRTeam team, PotionEffectType type) {
        for (UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, 0, false, false));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
        }
    }

    private void broadcastTeamMessage(TTRTeam team, String msg) {
        for (UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) p.sendMessage(msg);
        }
    }

    private int getPriceFromLore(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return -1;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("Costo:")) {
                String clean = ChatColor.stripColor(line).replaceAll("[^0-9]", "");
                try { return Integer.parseInt(clean); } catch (Exception e) { return -1; }
            }
        }
        return -1;
    }

    private Material getCurrencyFromLore(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("Costo:")) {
                if (line.contains("Esmeraldas")) return Material.EMERALD;
                if (line.contains("Carbón")) return Material.COAL;
            }
        }
        return null;
    }

    private void removeItems(Player p, Material type, int amount) {
        if (amount <= 0) return;
        for (int slot = 0; slot < p.getInventory().getSize(); slot++) {
            ItemStack is = p.getInventory().getItem(slot);
            if (is == null) continue;
            if (type == is.getType()) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    p.getInventory().clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!plugin.enabled()) return;
        if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.SHIELD) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onInventoryClickSelector(InventoryClickEvent event) {
        if (!plugin.enabled()) return;
        if (event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Seleccionar Equipo")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player p = (Player) event.getWhoClicked();
            Material type = event.getCurrentItem().getType();

            if (type == Material.RED_WOOL || type == Material.BLUE_WOOL) {
                String id = (type == Material.RED_WOOL) ? "Red" : "Blue";
                plugin.getTeamHandler().addPlayerToTeam(p, id);
                p.sendMessage(ChatColor.GREEN + "Unido a " + id);
                p.closeInventory();
                plugin.getScoreboard().updateAll();

                if (p.getGameMode() == GameMode.SPECTATOR) {
                    p.setGameMode(GameMode.ADVENTURE);
                    p.getInventory().clear();
                    plugin.getCurrentMatch().giveLobbyItems(p);
                    if (plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) {
                        p.teleport(plugin.getConfigManager().getLobbyLocation());
                    } else {
                        plugin.getCurrentMatch().joinPlayerToMatch(p);
                    }
                }
            } else if (type == Material.ENDER_EYE) {
                plugin.getTeamHandler().removePlayer(p);
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(ChatColor.GRAY + "Modo Espectador. Usa /ttrplay para volver.");
                p.getInventory().clear();
                p.closeInventory();
                plugin.getScoreboard().updateAll();
            }
        }
        if (plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.enabled()) return;
        if (event.getBlock().getType() == Material.BEACON) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "¡El Faro es indestructible!");
            return;
        }
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.enabled()) return;
        event.getDrops().clear();
        if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            plugin.getCurrentMatch().playerDeath(event.getEntity(), event.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.enabled()) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR) {
                new TeamSelector().open(event.getPlayer());
            }
            if (event.getItem() != null && event.getItem().getType() == Material.FIRE_CHARGE) {
                event.setCancelled(true);
                Player p = event.getPlayer();
                if (p.getGameMode() != GameMode.CREATIVE) {
                    p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
                }
                Fireball fb = p.launchProjectile(Fireball.class);
                fb.setYield(2.0F);
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.BEACON) {
                event.setCancelled(true);
                new BeaconShop().openMain(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.enabled() && plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.enabled()) return;
        MatchStatus status = plugin.getCurrentMatch().getStatus();

        Player p = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (status == MatchStatus.LOBBY) {
                    p.setGameMode(GameMode.ADVENTURE);
                    p.getInventory().clear();
                    plugin.getCurrentMatch().giveLobbyItems(p);
                    Location lobby = plugin.getConfigManager().getLobbyLocation();
                    if (lobby != null) p.teleport(lobby);
                    for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
                } else if (status == MatchStatus.INGAME) {
                    TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);
                    if (team != null) {
                        plugin.getCurrentMatch().joinPlayerToMatch(p);
                    } else {
                        p.setGameMode(GameMode.SPECTATOR);
                        p.getInventory().clear();
                        plugin.getCurrentMatch().giveLobbyItems(p);
                        Location lobby = plugin.getConfigManager().getLobbyLocation();
                        if (lobby != null) p.teleport(lobby);
                    }
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.enabled()) return;
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) return;
        Player player = event.getPlayer();
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);
        if (team != null) {
            Location teamSpawn = plugin.getConfigManager().getTeamSpawn(team.getIdentifier());
            if (teamSpawn != null) event.setRespawnLocation(teamSpawn);
            new BukkitRunnable() {
                @Override
                public void run() { plugin.getCurrentMatch().equipPlayer(player, team.getIdentifier()); }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.enabled()) return;
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player v = (Player) event.getEntity();
            Player a = (Player) event.getDamager();
            TTRTeam vt = plugin.getTeamHandler().getPlayerTeam(v);
            TTRTeam at = plugin.getTeamHandler().getPlayerTeam(a);
            if (vt != null && at != null && vt.getIdentifier().equals(at.getIdentifier())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.enabled()) return;
        if (event.getEntity() instanceof Player) {
            if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                event.setCancelled(true);
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    Location lobby = plugin.getConfigManager().getLobbyLocation();
                    if (lobby != null) event.getEntity().teleport(lobby);
                }
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!plugin.enabled()) return;
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }
}