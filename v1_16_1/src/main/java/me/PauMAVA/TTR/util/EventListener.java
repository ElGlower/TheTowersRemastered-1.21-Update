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

    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    // --- 1. PROTECCIÓN Y APERTURA DE TIENDA ---

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.enabled()) return;

        // ABRIR LA TIENDA (Click Derecho en el Faro)
        // Esto NO interfiere con BeaconShopListener, porque esto solo "Abre" el menú.
        // BeaconShopListener se encarga de lo que pasa "Dentro" del menú.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.BEACON) {
                event.setCancelled(true); // Para que no abran la interfaz del faro vanilla
                new BeaconShop().openMain(event.getPlayer());
                return;
            }
        }

        // Fuego / Bolas de Fuego
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.enabled()) return;

        // Proteger el Faro siempre
        if (event.getBlock().getType() == Material.BEACON) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "¡El Faro es indestructible!");
            return;
        }

        // Si la partida no ha empezado, no se rompe nada
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
        }
    }

    // --- 2. LÓGICA DE JUEGO (Muerte y Respawn) ---

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.enabled()) return;

        // IMPORTANTE: NO ponemos event.getDrops().clear() para que los ítems CAIGAN al suelo.
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

                // Devolvemos el kit y las mejoras guardadas
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getCurrentMatch().equipPlayer(player, team.getIdentifier());
                    }
                }.runTaskLater(plugin, 1L);
            }
        } else {
            // Respawn en Lobby
            Location lobby = plugin.getConfigManager().getLobbyLocation();
            if (lobby != null) event.setRespawnLocation(lobby);
        }
    }

    // --- 3. PROTECCIONES DE LOBBY ---

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
                // Si caen al vacío en el lobby, los subimos
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
        // Bloquear crafteo de escudos (para obligar a comprarlos)
        if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.SHIELD) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }
}