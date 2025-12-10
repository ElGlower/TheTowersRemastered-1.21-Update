package me.PauMAVA.TTR.match;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.lang.PluginString;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CageChecker {

    private List<Cage> cages = new ArrayList<>();
    private int checkerTaskPID;

    public void startChecking() {
        this.checkerTaskPID = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;

                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (p.getGameMode() == GameMode.SPECTATOR) continue;
                    if (cages == null || cages.isEmpty()) continue;

                    for (Cage cage : cages) {
                        if (cage == null || cage.getLocation() == null || cage.getLocation().getWorld() == null)
                            continue;
                        try {
                            Location particleLoc = cage.getLocation().clone().add(0.5, 1.0, 0.5);
                            cage.getLocation().getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0.3, 0.3, 0.3, 0.05);

                            if (cage.isInCage(p)) {
                                TTRTeam pTeam = TTRCore.getInstance().getTeamHandler().getPlayerTeam(p);
                                if (pTeam != null) {
                                    if (cage.getOwner().equals(pTeam)) {
                                        p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(0.5));
                                        p.sendMessage(ChatColor.RED + "¡No puedes entrar a tu propia jaula!");
                                    } else {
                                        cage.getLocation().getWorld().strikeLightningEffect(cage.getLocation());
                                        playerOnCage(p);
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }.runTaskTimer(TTRCore.getInstance(), 0L, 5L).getTaskId();
    }

    public void stopChecking() {
        if (Bukkit.getScheduler().isQueued(this.checkerTaskPID) || Bukkit.getScheduler().isCurrentlyRunning(this.checkerTaskPID)) {
            Bukkit.getScheduler().cancelTask(this.checkerTaskPID);
        }
    }

    private void playerOnCage(Player player) {
        TTRTeam playersTeam = TTRCore.getInstance().getTeamHandler().getPlayerTeam(player);
        if (playersTeam == null) return;

        Location spawn = TTRCore.getInstance().getConfigManager().getTeamSpawn(playersTeam.getIdentifier());
        if (spawn != null) player.teleport(spawn);

        playersTeam.addPoints(1);
        TTRCore.getInstance().getScoreboard().refreshScoreboard();
        TTRCore.getInstance().getCurrentMatch().updateBossBar();

        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GOLD + player.getName() + " ha anotado un punto!");
        for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);

        if (playersTeam.getPoints() >= TTRCore.getInstance().getCurrentMatch().getMaxPointsToWin()) {
            TTRCore.getInstance().getCurrentMatch().endMatch(playersTeam);
        }
    }

    public void setCages(HashMap<Location, TTRTeam> cages, int effectiveRadius) {
        if (cages == null) return;
        this.cages.clear();
        for (Location cage : cages.keySet()) {
            if (cage != null && cage.getWorld() != null) {
                this.cages.add(new Cage(cage, effectiveRadius, cages.get(cage)));
            }
        }
    }
}