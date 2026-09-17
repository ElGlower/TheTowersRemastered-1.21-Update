package me.PauMAVA.TTR.listeners;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Control de combate y prevención estricta de fuego amigo entre miembros del mismo equipo.
 */
public class TeamCombatListener implements Listener {

    private final TTRCore plugin;

    public TeamCombatListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    public TeamCombatListener() {
        this(TTRCore.getInstance());
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) return;

        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player shooter) {
                attacker = shooter;
            }
        }

        if (attacker == null) return;
        if (victim.equals(attacker)) return;

        TTRTeam victimTeam = plugin.getTeamHandler().getPlayerTeam(victim);
        TTRTeam attackerTeam = plugin.getTeamHandler().getPlayerTeam(attacker);

        if (victimTeam != null && attackerTeam != null) {
            if (victimTeam.getIdentifier().equalsIgnoreCase(attackerTeam.getIdentifier())) {
                event.setCancelled(true);
                return;
            }

            // Spawn-kill Protection: Prohibido atacar a jugadores dentro de su propia base/spawn
            org.bukkit.Location victimSpawn = plugin.getConfigManager().getTeamSpawn(victimTeam.getIdentifier());
            if (victimSpawn != null && victimSpawn.getWorld() != null && victimSpawn.getWorld().equals(victim.getWorld())) {
                if (victimSpawn.distance(victim.getLocation()) <= 9.0) {
                    event.setCancelled(true);
                    attacker.sendMessage(me.PauMAVA.TTR.util.TTRPrefix.TTR_ERROR + me.PauMAVA.TTR.util.TextUtil.toTiny("¡Spawn-Kill prohibido! No puedes atacar a jugadores en su base."));
                    attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
                    return;
                }
            }
        }
    }
}