package me.PauMAVA.TTR.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.Team;

public class TeamCombatListener implements Listener {

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Player attacker = null;

        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        if (attacker == null) return;

        if (victim.equals(attacker)) return;
        if (areOnSameTeam(victim, attacker)) {
            event.setCancelled(true);
        }
    }

    private boolean areOnSameTeam(Player p1, Player p2) {
        Team team1 = p1.getScoreboard().getEntryTeam(p1.getName());
        Team team2 = p2.getScoreboard().getEntryTeam(p2.getName());

        if (team1 == null || team2 == null) return false;

        return team1.getName().equals(team2.getName());
    }
}