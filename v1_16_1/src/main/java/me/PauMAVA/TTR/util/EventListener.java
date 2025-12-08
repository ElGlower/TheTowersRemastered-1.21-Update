package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.BeaconShop;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {

    private final TTRCore plugin;

    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    // --- INTERACCIÓN CON EL FARO (TIENDA) ---
    @EventHandler
    public void onInteractBeacon(PlayerInteractEvent event) {
        if (!plugin.enabled()) return;

        // Si hace clic derecho en un BEACON
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.BEACON) {
                // Cancelamos la interfaz normal del faro
                event.setCancelled(true);
                // Abrimos nuestra tienda
                new BeaconShop().openShop(event.getPlayer());
            }
        }
    }

    // --- PROTECCIÓN DEL FARO (INDESTRUCTIBLE) ---
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.enabled()) return;

        // Nadie puede romper Faros
        if (event.getBlock().getType() == Material.BEACON) {
            if (!event.getPlayer().isOp()) { // Opcional: Dejar que los OP rompan si están en creativo
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "¡El Faro es indestructible!");
            }
        }
    }

    // --- ANTI-FUEGO AMIGO Y PROTECCIÓN LOBBY ---
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.enabled()) return;

        // Si no estamos jugando (Lobby/Fin), nadie se pega
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
            return;
        }

        // Lógica de Fuego Amigo
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = null;

            // Detectar atacante (Directo o Flecha)
            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile proj = (Projectile) event.getDamager();
                if (proj.getShooter() instanceof Player) {
                    attacker = (Player) proj.getShooter();
                }
            }

            //  hay atacante y víctima, verificar equipos
            if (attacker != null && attacker != victim) {
                TTRTeam victimTeam = plugin.getTeamHandler().getPlayerTeam(victim);
                TTRTeam attackerTeam = plugin.getTeamHandler().getPlayerTeam(attacker);

                // Si son del mismo equipo, cancelar daño
                if (victimTeam != null && attackerTeam != null) {
                    if (victimTeam.getIdentifier().equals(attackerTeam.getIdentifier())) {
                        event.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "¡No puedes atacar a tu equipo!");
                    }
                }
            }
        }
    }

    // --- PREVENIR DAÑO EN LOBBY (Caída, lava, etc.) ---
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.enabled()) return;
        if (event.getEntity() instanceof Player) {
            if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                event.setCancelled(true);
            }
        }
    }

    // --- PREVENIR HAMBRE EN LOBBY ---
    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!plugin.enabled()) return;
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }
}