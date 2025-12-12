package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.BeaconShop;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {

    private final TTRCore plugin;
    // Radio de protección (bloques). Nadie puede romper/poner nada aquí.
    private final int PROTECTION_RADIUS = 6;

    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    // --- PROTECCIÓN DE CONSTRUCCIÓN ---

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.enabled()) return;
        Player p = event.getPlayer();

        // 1. Proteger Faro
        if (event.getBlock().getType() == Material.BEACON) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "¡El Faro es indestructible!");
            return;
        }

        // 2. Si no ha empezado, nadie rompe nada (excepto admin creativo)
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            if (p.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
            return;
        }

        // 3. PROTECCIÓN DE SPAWNS (Universal)
        if (isSpawnZone(event.getBlock().getLocation())) {
            if (p.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + "¡No puedes romper bloques en la zona de Spawn!");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.enabled()) return;

        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
            return;
        }

        // 3. PROTECCIÓN DE SPAWNS (Universal)
        if (isSpawnZone(event.getBlock().getLocation())) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "¡No puedes construir en la zona de Spawn!");
            }
        }
    }

    // Método que revisa si el bloque está cerca de ALGÚN spawn (Rojo o Azul)
    private boolean isSpawnZone(Location blockLoc) {
        // Revisar todos los equipos configurados
        for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
            Location spawn = plugin.getConfigManager().getTeamSpawn(team.getIdentifier());

            // Si el spawn existe y está en el mismo mundo
            if (spawn != null && spawn.getWorld().equals(blockLoc.getWorld())) {
                // Si la distancia es menor al radio protegido
                if (spawn.distance(blockLoc) <= PROTECTION_RADIUS) {
                    return true;
                }
            }
        }
        return false;
    }

    // --- INTERACCIONES ---
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.enabled()) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.BEACON) {
                event.setCancelled(true);
                new BeaconShop().openMain(event.getPlayer());
                return;
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
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
    }

    // --- MUERTE Y RESPAWN ---
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.enabled()) return;

        // DROPS HABILITADOS (Se caen al suelo)
        event.setDroppedExp(0);

        if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            plugin.getCurrentMatch().playerDeath(event.getEntity(), event.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.enabled()) return;

        if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            Player player = event.getPlayer();
            TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

            if (team != null) {
                Location teamSpawn = plugin.getConfigManager().getTeamSpawn(team.getIdentifier());
                if (teamSpawn != null) event.setRespawnLocation(teamSpawn);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getCurrentMatch().equipPlayer(player, team.getIdentifier());
                    }
                }.runTaskLater(plugin, 1L);
            }
        } else {
            Location lobby = plugin.getConfigManager().getLobbyLocation();
            if (lobby != null) event.setRespawnLocation(lobby);
        }
    }

    // --- PROTECCIONES VARIAS ---
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.enabled() && plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) {
            event.setCancelled(true);
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

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!plugin.enabled()) return;
        if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.SHIELD) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }
}