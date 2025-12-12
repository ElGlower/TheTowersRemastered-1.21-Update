package me.PauMAVA.TTR.listeners;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TeamCombatListener implements Listener {

    private final TTRCore plugin;

    public TeamCombatListener(TTRCore plugin) {
        this.plugin = plugin; // Ahora recibe el plugin para acceder al TeamHandler
    }

    // Constructor vacío por si acaso (aunque deberías usar el de arriba en TTRCore)
    public TeamCombatListener() {
        this.plugin = TTRCore.getInstance();
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        // Solo nos importa si estamos en partida
        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) return;

        // 1. Verificar víctima
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Player attacker = null;

        // 2. Determinar atacante (Melee o Proyectil)
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        if (attacker == null) return;
        if (victim.equals(attacker)) return; // Permitir dañarse a uno mismo (ej. ender pearl)

        // 3. VERIFICACIÓN ROBUSTA DE EQUIPOS USANDO EL HANDLER
        TTRTeam victimTeam = plugin.getTeamHandler().getPlayerTeam(victim);
        TTRTeam attackerTeam = plugin.getTeamHandler().getPlayerTeam(attacker);

        // Si ambos tienen equipo y es EL MISMO -> CANCELAR
        if (victimTeam != null && attackerTeam != null) {
            if (victimTeam.getIdentifier().equals(attackerTeam.getIdentifier())) {
                event.setCancelled(true);
            }
        }
    }
}