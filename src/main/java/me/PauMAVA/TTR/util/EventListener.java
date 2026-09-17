package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.BeaconShop;
import me.PauMAVA.TTR.ui.ConfigGUI;
import me.PauMAVA.TTR.listeners.TeamSelectListener;
import org.bukkit.ChatColor;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
            if (plugin.isBeaconShopEnabled()) {
                event.setCancelled(true);
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡El Faro con tienda es indestructible!"));
                return;
            } else {
                if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                    if (p.getGameMode() != GameMode.CREATIVE) {
                        event.setCancelled(true);
                        p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes romper faros fuera de la partida!"));
                        return;
                    }
                }
            }
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

        if (isCageZone(event.getBlock().getLocation())) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes colocar bloques dentro de las jaulas!"));
                return;
            }
        }

        if (isSpawnZone(event.getBlock().getLocation())) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes construir en la zona de Spawn!"));
            }
        }
    }

    private boolean isCageZone(Location blockLoc) {
        for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
            List<Location> cages = plugin.getConfigManager().getTeamCages(team.getIdentifier());
            if (cages != null) {
                for (Location cage : cages) {
                    if (cage != null && cage.getWorld() != null && cage.getWorld().equals(blockLoc.getWorld())) {
                        if (cage.distance(blockLoc) <= 3.5) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
                if (!plugin.isBeaconShopEnabled()) {
                    event.getPlayer().sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("La tienda del faro está desactivada por la administración."));
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
                    return;
                }
                new BeaconShop().openMain(event.getPlayer());
                return;
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null) {
                if (event.getItem().getType() == Material.COMPARATOR && (event.getPlayer().hasPermission("destinytowers.admin") || event.getPlayer().hasPermission("ttr.admin") || event.getPlayer().isOp())) {
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

    @EventHandler
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        if (!plugin.enabled()) return;
        Player p = event.getPlayer();
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);
        boolean isStaff = p.isOp() || p.hasPermission("destinytowers.admin") || p.hasPermission("ttr.admin");
        String staffPrefix = isStaff ? me.PauMAVA.TTR.ui.DestinyTheme.DESTINY_ROLE_BADGE + " " : "";

        if (team != null && team.isLeader(p.getUniqueId())) {
            String leaderBadge = ChatColor.GOLD + "★ [" + team.getColor() + TextUtil.toTiny("Líder ") + team.getColor() + TextUtil.toTiny(team.getIdentifier()) + ChatColor.GOLD + "] " + ChatColor.RESET;
            event.setFormat(staffPrefix + leaderBadge + team.getColor() + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        } else if (team != null) {
            String teamPrefix = ChatColor.DARK_GRAY + "[" + team.getColor() + TextUtil.toTiny(team.getIdentifier()) + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
            event.setFormat(staffPrefix + teamPrefix + team.getColor() + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        } else if (isStaff) {
            event.setFormat(staffPrefix + ChatColor.WHITE + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        }
    }
}
