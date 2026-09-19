package me.PauMAVA.TTR.modes;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.LeaderVoteGUI;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderVoteManager {

    public static class TeamVoteState {
        private final TTRTeam team;
        private int round = 1;
        private final List<UUID> candidates = new ArrayList<>();
        private final Map<UUID, UUID> votes = new ConcurrentHashMap<>();
        private boolean finished = false;
        private UUID electedLeader = null;

        public TeamVoteState(TTRTeam team) {
            this.team = team;
            for (UUID uuid : team.getPlayers()) {
                if (!TTRCore.isAdmin(uuid)) {
                    this.candidates.add(uuid);
                }
            }
        }

        public TTRTeam getTeam() { return team; }
        public int getRound() { return round; }
        public List<UUID> getCandidates() { return candidates; }
        public Map<UUID, UUID> getVotes() { return votes; }
        public boolean isFinished() { return finished; }
        public UUID getElectedLeader() { return electedLeader; }
        public void setElectedLeader(UUID uuid) { this.electedLeader = uuid; this.finished = true; }

        public int getVotesFor(UUID candidate) {
            int count = 0;
            for (UUID target : votes.values()) {
                if (target.equals(candidate)) count++;
            }
            return count;
        }
    }

    private boolean active = false;
    private int secondsRemaining = 20;
    private BukkitTask voteTask = null;
    private final Map<String, TeamVoteState> teamStates = new ConcurrentHashMap<>();

    public boolean isActive() {
        return active;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public int getRoundDuration() {
        return TTRCore.getInstance().getConfig().getInt("voting.round_seconds", 20);
    }

    public TeamVoteState getState(String teamId) {
        return teamStates.get(teamId.toLowerCase());
    }

    public void startVoting(boolean force) {
        TTRCore plugin = TTRCore.getInstance();
        cancelVoting();

        List<Player> playing = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!TTRCore.isAdmin(p)) {
                playing.add(p);
            }
        }

        if (playing.size() < 2) {
            Bukkit.broadcastMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Se requieren al menos 2 jugadores (no administradores) para iniciar la fase."));
            return;
        }

        TTRTeam red = plugin.getTeamHandler().getTeam("Red");
        TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");

        // Caso especial 2 jugadores: 1v1 automático, coronar líderes de inmediato
        if (playing.size() == 2) {
            if (red != null && blue != null) {
                Player p1 = playing.get(0);
                Player p2 = playing.get(1);
                plugin.getTeamHandler().setPlayerTeam(p1, red);
                plugin.getTeamHandler().setPlayerTeam(p2, blue);
                crownLeader(red, p1.getUniqueId());
                crownLeader(blue, p2.getUniqueId());
                Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + TextUtil.toTiny("¡Duelo 1v1! Los 2 jugadores son capitanes de sus respectivos equipos."));

                // Iniciar directamente conteo pre-partida
                int prestart = plugin.getConfig().getInt("match.prestart_countdown", 10);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getAutoStarter().startMatchCountdown(prestart);
                }, 40L);
                return;
            }
        }

        // Si los equipos están vacíos (jugadores en lobby), repartir equitativamente SOLO jugadores participantes
        if (red != null && blue != null && red.getPlayers().isEmpty() && blue.getPlayers().isEmpty()) {
            for (int i = 0; i < playing.size(); i++) {
                if (i % 2 == 0) {
                    plugin.getTeamHandler().setPlayerTeam(playing.get(i), red);
                } else {
                    plugin.getTeamHandler().setPlayerTeam(playing.get(i), blue);
                }
            }
        }

        teamStates.clear();
        for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
            TeamVoteState state = new TeamVoteState(team);
            if (state.getCandidates().isEmpty()) {
                state.finished = true;
            } else if (state.getCandidates().size() == 1) {
                state.setElectedLeader(state.getCandidates().get(0));
            }
            teamStates.put(team.getIdentifier().toLowerCase(), state);
        }

        // Check if all already done
        boolean allDone = true;
        for (TeamVoteState s : teamStates.values()) {
            if (!s.isFinished()) {
                allDone = false;
                break;
            }
        }

        if (allDone) {
            concludeVoting();
            return;
        }

        this.active = true;
        this.secondsRemaining = getRoundDuration();

        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.GOLD + "" + ChatColor.BOLD +
                TextUtil.toTiny("¡Comienza la votación de líderes por rondas!"));
        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.GRAY +
                TextUtil.toTiny("Vota a tu compañero de equipo para líder (Ronda 1)."));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TTRCore.isAdmin(p)) continue; // Administradores no participan en votación
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
            TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);
            if (team != null) {
                TeamVoteState state = getState(team.getIdentifier());
                if (state != null) {
                    LeaderVoteGUI.open(p, state);
                }
            }
        }

        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }

                secondsRemaining--;

                // Actionbar con segundos para los votantes
                String ab = TextUtil.color("&#FFFFFF⏱ " + TextUtil.toTiny("Tiempo para votar: ") + "&#FF2E2E§l" + secondsRemaining + "s");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TTRTeam t = plugin.getTeamHandler().getPlayerTeam(p);
                    if (t != null) {
                        TeamVoteState s = getState(t.getIdentifier());
                        if (s != null && !s.isFinished()) {
                            p.sendActionBar(net.kyori.adventure.text.Component.text(ab));
                            LeaderVoteGUI.updateTimer(p, secondsRemaining);
                        }
                    }
                }

                if (secondsRemaining <= 0) {
                    processRounds();
                } else if (secondsRemaining <= 5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.8f);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public boolean castVote(Player voter, UUID candidate) {
        if (!active) return false;
        if (TTRCore.isAdmin(voter)) {
            voter.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Los administradores no participan en las votaciones."));
            return false;
        }
        TTRCore plugin = TTRCore.getInstance();
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(voter);
        if (team == null) return false;

        TeamVoteState state = getState(team.getIdentifier());
        if (state == null || state.isFinished()) return false;

        if (!state.getCandidates().contains(candidate)) return false;

        state.getVotes().put(voter.getUniqueId(), candidate);
        voter.playSound(voter.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);

        Player target = Bukkit.getPlayer(candidate);
        String name = (target != null) ? target.getName() : "jugador";
        voter.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Has votado por ") + ChatColor.YELLOW + name);

        // Check if all team members have voted (excluding admins)
        int totalOnline = 0;
        for (UUID member : state.getTeam().getPlayers()) {
            if (!TTRCore.isAdmin(member) && Bukkit.getPlayer(member) != null) totalOnline++;
        }
        if (state.getVotes().size() >= totalOnline && totalOnline > 0) {
            checkEarlyRoundAdvance(state);
        }

        return true;
    }

    private void checkEarlyRoundAdvance(TeamVoteState state) {
        boolean allFinishedOrEarly = true;
        for (TeamVoteState s : teamStates.values()) {
            if (s.isFinished()) continue;
            int totalOnline = 0;
            for (UUID member : s.getTeam().getPlayers()) {
                if (!TTRCore.isAdmin(member) && Bukkit.getPlayer(member) != null) totalOnline++;
            }
            if (s.getVotes().size() < totalOnline) {
                allFinishedOrEarly = false;
                break;
            }
        }

        if (allFinishedOrEarly) {
            processRounds();
        }
    }

    private void processRounds() {
        boolean anyTeamAdvanced = false;

        for (TeamVoteState state : teamStates.values()) {
            if (state.isFinished()) continue;

            List<UUID> current = new ArrayList<>(state.getCandidates());
            if (current.isEmpty()) {
                state.finished = true;
                continue;
            }
            if (current.size() == 1) {
                state.setElectedLeader(current.get(0));
                continue;
            }

            // Sort candidates by votes descending
            current.sort((a, b) -> Integer.compare(state.getVotesFor(b), state.getVotesFor(a)));

            if (current.size() == 2) {
                // Final round
                int v0 = state.getVotesFor(current.get(0));
                int v1 = state.getVotesFor(current.get(1));
                if (v0 >= v1) {
                    state.setElectedLeader(current.get(0));
                } else {
                    state.setElectedLeader(current.get(1));
                }
                anyTeamAdvanced = true;
            } else {
                // More than 2 candidates: filter down
                // If more than 2, keep top candidates (at most 2 for next round)
                List<UUID> nextCandidates = new ArrayList<>();
                nextCandidates.add(current.get(0));
                nextCandidates.add(current.get(1));

                state.getCandidates().clear();
                state.getCandidates().addAll(nextCandidates);
                state.getVotes().clear();
                state.round++;
                anyTeamAdvanced = true;

                Player c1 = Bukkit.getPlayer(nextCandidates.get(0));
                Player c2 = Bukkit.getPlayer(nextCandidates.get(1));
                String name1 = (c1 != null) ? c1.getName() : "Finalista 1";
                String name2 = (c2 != null) ? c2.getName() : "Finalista 2";

                state.getTeam().getPlayers().forEach(uuid -> {
                    if (TTRCore.isAdmin(uuid)) return;
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(TTRPrefix.TTR_GAME + ChatColor.AQUA +
                                TextUtil.toTiny("Ronda ") + state.getRound() + TextUtil.toTiny(" (Final): ") +
                                ChatColor.YELLOW + name1 + ChatColor.WHITE + " vs " + ChatColor.YELLOW + name2);
                        LeaderVoteGUI.open(p, state);
                    }
                });
            }
        }

        // Check if all teams are finished
        boolean allDone = true;
        for (TeamVoteState s : teamStates.values()) {
            if (!s.isFinished()) {
                allDone = false;
                break;
            }
        }

        if (allDone) {
            concludeVoting();
        } else {
            // Reset round timer for remaining teams
            this.secondsRemaining = getRoundDuration();
            Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.GOLD +
                    TextUtil.toTiny("¡Siguiente ronda de votación iniciada! Tienes " + secondsRemaining + "s."));
        }
    }

    public void concludeVoting() {
        cancelTaskOnly();
        this.active = false;

        TTRCore plugin = TTRCore.getInstance();
        for (TeamVoteState state : teamStates.values()) {
            if (state.getElectedLeader() != null) {
                crownLeader(state.getTeam(), state.getElectedLeader());
            }
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "★ " +
                TextUtil.toTiny("Los líderes del equipo han sido seleccionados") + " ★");
        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            if (p.getOpenInventory().getTitle().contains(TextUtil.toTiny("Votación de Líder"))) {
                p.closeInventory();
            }
        }

        // Si la modalidad activa es Subasta (Auction Draft), transicionar a subasta
        if (plugin.getCurrentSelectionMode() == TeamSelectionMode.AUCTION_DRAFT) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getAuctionDraftManager().startDraft();
            }, 40L);
        } else {
            int prestart = plugin.getConfig().getInt("match.prestart_countdown", 10);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getAutoStarter().startMatchCountdown(prestart);
            }, 40L);
        }
    }

    public void crownLeader(TTRTeam team, UUID leaderUuid) {
        team.setLeader(leaderUuid);
        Player leader = Bukkit.getPlayer(leaderUuid);
        String name = (leader != null) ? leader.getName() : "Desconocido";

        String roleTitle = team.getColor() + "★ " + TextUtil.toTiny("Líder ") + team.getColor() + TextUtil.toTiny(team.getIdentifier());
        Bukkit.broadcastMessage(TTRPrefix.TTR_SUCCESS + roleTitle + ChatColor.WHITE + " » " + ChatColor.YELLOW + name);

        if (leader != null) {
            leader.sendTitle(ChatColor.GOLD + "★ " + TextUtil.toTiny("¡ERES EL LÍDER!"),
                    team.getColor() + TextUtil.toTiny("Dirige a tu equipo hacia la victoria"), 10, 60, 20);
            leader.playSound(leader.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
        }
    }

    public void addSeconds(int s) {
        if (!active) return;
        this.secondsRemaining += s;
        if (this.secondsRemaining < 0) this.secondsRemaining = 0;
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Tiempo de votación modificado: ") +
                (s >= 0 ? ChatColor.GREEN + "+" + s : ChatColor.RED + "" + s) + "s" +
                ChatColor.GRAY + " (" + ChatColor.WHITE + secondsRemaining + "s restantes" + ChatColor.GRAY + ")");
    }

    public void setSeconds(int s) {
        if (!active) return;
        this.secondsRemaining = Math.max(0, s);
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Tiempo de votación establecido en: ") +
                ChatColor.GREEN + s + "s");
    }

    public void forceNextRound() {
        if (!active) return;
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("¡Ronda de votación acelerada por Destiny!"));
        processRounds();
    }

    public void cancelVoting() {
        cancelTaskOnly();
        this.active = false;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTitle().contains(TextUtil.toTiny("Votación de Líder"))) {
                p.closeInventory();
            }
        }
    }

    private void cancelTaskOnly() {
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }
    }
}
