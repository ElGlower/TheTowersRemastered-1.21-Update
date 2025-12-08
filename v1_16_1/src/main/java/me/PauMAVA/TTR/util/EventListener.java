/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 * [Licencia...]
 */
package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.TeamSelector;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import me.PauMAVA.TTR.ui.BeaconShop;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class EventListener implements Listener {

    private final TTRCore plugin;

    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.enabled()) return;

        Player player = event.getPlayer();
        plugin.getPacketInterceptor().addPlayer(player);
        event.setJoinMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "+ " + ChatColor.GRAY + player.getName());

        MatchStatus status = plugin.getCurrentMatch().getStatus();

        if (status == MatchStatus.PREGAME) {
            sendToLobby(player);
        } else if (status == MatchStatus.INGAME) {
            TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

            if (team != null) {
                player.sendMessage(ChatColor.GREEN + "¡Has vuelto a la partida!");
                if (plugin.getCurrentMatch().getBossBar() != null) {
                    plugin.getCurrentMatch().getBossBar().addPlayer(player);
                }
            } else {
                sendToLobby(player);
                player.sendMessage(ChatColor.YELLOW + "La partida está en curso. Usa la Estrella para unirte.");
            }
        }
    }

    private void sendToLobby(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        ItemStack selector = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = selector.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Elegir Equipo " + ChatColor.GRAY + "(Clic Derecho)");
            selector.setItemMeta(meta);
        }
        inv.setItem(4, selector);

        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);

        Location loc = plugin.getConfigManager().getLobbyLocation();
        if (loc != null && loc.getWorld() != null) player.teleport(loc);

        plugin.getAutoStarter().addPlayerToQueue(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (plugin.enabled()) {
            event.setQuitMessage(TTRPrefix.TTR_GAME + " " + ChatColor.RED + "- " + ChatColor.GRAY + event.getPlayer().getName());
            plugin.getAutoStarter().removePlayerFromQueue(event.getPlayer());
            plugin.getPacketInterceptor().removePlayer(event.getPlayer());
        }
    }

    // --- PROTECCIONES DE LOBBY ---
    private boolean canInteract(Player p) {
        return plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME &&
                plugin.getTeamHandler().getPlayerTeam(p) != null;
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent event) {
        if (plugin.enabled() && !canInteract(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.enabled() && event.getWhoClicked() instanceof Player) {
            if (!canInteract((Player) event.getWhoClicked())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreativeEvent(InventoryCreativeEvent event) {
        if (plugin.enabled() && event.getWhoClicked() instanceof Player) {
            if (!canInteract((Player) event.getWhoClicked())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerClickEvent(PlayerInteractEvent event) {
        if (!plugin.enabled()) return;
        if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR) {
            new TeamSelector(event.getPlayer()).openSelector();
            event.setCancelled(true);
            return;
        }
        if (!canInteract(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void placeBlockEvent(BlockPlaceEvent event) {
        if (plugin.enabled() && !canInteract(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void breakBlockEvent(BlockBreakEvent event) {
        if (plugin.enabled() && !canInteract(event.getPlayer())) event.setCancelled(true);
    }

    // --- NUEVO: BLOQUEO MANUAL DE FUEGO AMIGO ---
    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!plugin.enabled()) return;

        // Verificar si la víctima es un jugador
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Player attacker = null;

        // Caso 1: Golpe directo
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }
        // Caso 2: Flechazo o Bola de Nieve
        else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        // Si hay atacante y víctima
        if (attacker != null && attacker != victim) {
            // Verificar si ambos tienen equipo
            TTRTeam vTeam = plugin.getTeamHandler().getPlayerTeam(victim);
            TTRTeam aTeam = plugin.getTeamHandler().getPlayerTeam(attacker);

            if (vTeam != null && aTeam != null) {
                // Si son del mismo equipo -> BLOQUEAR
                if (vTeam.getIdentifier().equals(aTeam.getIdentifier())) {
                    event.setCancelled(true);
                    attacker.sendMessage(ChatColor.RED + "¡No puedes atacar a tu equipo!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (plugin.enabled() && !canInteract(p)) {
                event.setCancelled(true); // Nadie recibe daño en el Lobby
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.enabled() && plugin.getCurrentMatch().isOnCourse()) {
            if (plugin.getTeamHandler().getPlayerTeam(event.getEntity()) != null) {
                plugin.getCurrentMatch().playerDeath(event.getEntity(), event.getEntity().getKiller());
                event.setDeathMessage(null);
                event.getDrops().clear();
            }
        }
    }

    @EventHandler
    public void onInteractBeacon(PlayerInteractEvent event) {
        if (!plugin.enabled()) return;

        // Si hace clic derecho en un BEACON
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.BEACON) {
                // Cancelamos la interfaz vanilla del faro
                event.setCancelled(true);

                // Abrimos nuestra tienda
                new BeaconShop().openShop(event.getPlayer());
            }
        }
    }
}