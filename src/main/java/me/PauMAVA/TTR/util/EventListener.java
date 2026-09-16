package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.BeaconShop;
import me.PauMAVA.TTR.ui.ConfigGUI;
import me.PauMAVA.TTR.listeners.TeamSelectListener;
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
    private final int PROTECTION_RADIUS = 6;

    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.enabled()) return;
        Player p = event.getPlayer();

        if (event.getBlock().getType() == Material.BEACON) {
            event.setCancelled(true);
            p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡El Faro es indestructible!"));
            return;
        }

        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            if (p.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
            return;
        }

        if (isSpawnZone(event.getBlock().getLocation())) {
            if (p.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes romper bloques en la zona de Spawn!"));
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

        if (isSpawnZone(event.getBlock().getLocation())) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes construir en la zona de Spawn!"));
            }
        }
    }

    private boolean isSpawnZone(Location blockLoc) {
        for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
            Location spawn = plugin.getConfigManager().getTeamSpawn(team.getIdentifier());
            if (spawn != null && spawn.getWorld() != null && spawn.getWorld().equals(blockLoc.getWorld())) {
                if (spawn.distance(blockLoc) <= PROTECTION_RADIUS) {
                    return true;
                }
            }
        }
        return false;
    }

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
            if (event.getItem() != null) {
                if (event.getItem().getType() == Material.COMPARATOR && event.getPlayer().hasPermission("ttr.admin")) {
                    event.setCancelled(true);
                    ConfigGUI.open(event.getPlayer());
                    return;
                }
                if (event.getItem().getType() == Material.NETHER_STAR && plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) {
                    event.setCancelled(true);
                    new TeamSelectListener(plugin).openTeamSelection(event.getPlayer());
                    return;
                }
                if (event.getItem().getType() == Material.FIRE_CHARGE) {
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
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.enabled()) return;
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

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.enabled() && plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.enabled()) return;
        if (event.getEntity() instanceof Player player) {
            if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                event.setCancelled(true);
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    Location lobby = plugin.getConfigManager().getLobbyLocation();
                    if (lobby != null) player.teleport(lobby);
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
