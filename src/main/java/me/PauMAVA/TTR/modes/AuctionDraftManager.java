package me.PauMAVA.TTR.modes;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.AuctionDraftGUI;
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

public class AuctionDraftManager {

    private boolean active = false;
    private final List<UUID> pool = new ArrayList<>();
    private UUID currentCandidate = null;
    private int currentBid = 0;
    private String highestBidderTeam = null;
    private UUID highestBidderUuid = null;
    private int secondsRemaining = 15;
    private final int TURN_DURATION = 15;

    private final Map<String, Integer> credits = new ConcurrentHashMap<>();
    private final Map<String, Boolean> passed = new ConcurrentHashMap<>();
    private BukkitTask draftTask = null;

    public boolean isActive() { return active; }
    public UUID getCurrentCandidate() { return currentCandidate; }
    public int getCurrentBid() { return currentBid; }
    public String getHighestBidderTeam() { return highestBidderTeam; }
    public int getSecondsRemaining() { return secondsRemaining; }
    public int getTeamCredits(String team) { return credits.getOrDefault(team.toLowerCase(), 100); }
    public boolean hasPassed(String team) { return passed.getOrDefault(team.toLowerCase(), false); }

    public void startDraft() {
        TTRCore plugin = TTRCore.getInstance();
        cancelDraft();

        credits.put("red", 100);
        credits.put("blue", 100);
        passed.put("red", false);
        passed.put("blue", false);

        // Ensure leaders exist
        ensureLeaders();

        // Populate pool with players who are not leaders
        pool.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);
            if (team != null && team.isLeader(p.getUniqueId())) {
                continue; // Leaders don't get auctioned
            }
            pool.add(p.getUniqueId());
        }

        if (pool.isEmpty()) {
            Bukkit.broadcastMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No hay suficientes jugadores en cola para subastar."));
            return;
        }

        Collections.shuffle(pool);
        this.active = true;

        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "⚖ " +
                TextUtil.toTiny("¡COMIENZA LA SUBASTA DE JUGADORES (AUCTION DRAFT)!") + " ⚖");
        Bukkit.broadcastMessage(ChatColor.GRAY + TextUtil.toTiny("Cada capitán tiene 100 créditos para pujar por sus compañeros."));
        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        nextCandidate();

        draftTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }

                secondsRemaining--;
                if (secondsRemaining <= 0 || (hasPassed("red") && hasPassed("blue"))) {
                    resolveCurrentAuction();
                } else {
                    refreshGUI();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void ensureLeaders() {
        TTRCore plugin = TTRCore.getInstance();
        TTRTeam red = plugin.getTeamHandler().getTeam("Red");
        TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");

        if (red != null && red.getLeader() == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                red.setLeader(p.getUniqueId());
                red.addPlayer(p);
                break;
            }
        }
        if (blue != null && blue.getLeader() == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (red != null && !p.getUniqueId().equals(red.getLeader())) {
                    blue.setLeader(p.getUniqueId());
                    blue.addPlayer(p);
                    break;
                }
            }
        }
    }

    private void nextCandidate() {
        if (pool.isEmpty()) {
            concludeDraft();
            return;
        }

        currentCandidate = pool.remove(0);
        currentBid = 0;
        highestBidderTeam = null;
        highestBidderUuid = null;
        secondsRemaining = TURN_DURATION;
        passed.put("red", false);
        passed.put("blue", false);

        Player candidatePlayer = Bukkit.getPlayer(currentCandidate);
        String name = (candidatePlayer != null) ? candidatePlayer.getName() : "Jugador";

        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "" + ChatColor.BOLD +
                TextUtil.toTiny("En subasta: ") + ChatColor.WHITE + name +
                ChatColor.GRAY + TextUtil.toTiny(" | Puja inicial: 0 créditos. Tienes 15s."));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        }

        refreshGUI();
    }

    private void resolveCurrentAuction() {
        TTRCore plugin = TTRCore.getInstance();
        Player candidate = Bukkit.getPlayer(currentCandidate);
        String candidateName = (candidate != null) ? candidate.getName() : "Jugador";

        if (highestBidderTeam != null && candidate != null) {
            // Deduct credits
            int prev = getTeamCredits(highestBidderTeam);
            credits.put(highestBidderTeam, Math.max(0, prev - currentBid));

            // Assign player
            plugin.getTeamHandler().addPlayerToTeam(candidate, highestBidderTeam);
            TTRTeam wonTeam = plugin.getTeamHandler().getTeam(highestBidderTeam);
            ChatColor color = (wonTeam != null) ? wonTeam.getColor() : ChatColor.WHITE;

            Bukkit.broadcastMessage(TTRPrefix.TTR_SUCCESS + color + "" + ChatColor.BOLD +
                    candidateName + ChatColor.GREEN + TextUtil.toTiny(" fichado por el Equipo ") +
                    color + TextUtil.toTiny(highestBidderTeam.toUpperCase()) +
                    ChatColor.YELLOW + " (" + currentBid + " créditos)");
        } else if (candidate != null) {
            // Nobody bid, assign to team with fewer players
            TTRTeam red = plugin.getTeamHandler().getTeam("Red");
            TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");
            int rCount = (red != null) ? red.getPlayers().size() : 0;
            int bCount = (blue != null) ? blue.getPlayers().size() : 0;
            String assigned = (rCount <= bCount) ? "Red" : "Blue";

            plugin.getTeamHandler().addPlayerToTeam(candidate, assigned);
            TTRTeam team = plugin.getTeamHandler().getTeam(assigned);
            ChatColor col = (team != null) ? team.getColor() : ChatColor.WHITE;

            Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.GRAY +
                    TextUtil.toTiny("Sin pujas por ") + candidateName +
                    TextUtil.toTiny(". Asignado al azar a ") + col + TextUtil.toTiny(assigned));
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        }

        nextCandidate();
    }

    public boolean bid(Player captain, int amountToAdd) {
        if (!active || currentCandidate == null) return false;
        TTRCore plugin = TTRCore.getInstance();
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(captain);

        if (team == null || !team.isLeader(captain.getUniqueId())) {
            // Allow OP as fallback
            if (!captain.isOp()) {
                captain.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Solo los capitanes de equipo pueden pujar."));
                return false;
            }
        }

        String teamId = (team != null) ? team.getIdentifier().toLowerCase() : "red";
        int available = getTeamCredits(teamId);
        int newBid = currentBid + amountToAdd;

        if (newBid > available) {
            captain.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Créditos insuficientes! Disponibles: ") + available);
            captain.playSound(captain.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
            return false;
        }

        currentBid = newBid;
        highestBidderTeam = teamId;
        highestBidderUuid = captain.getUniqueId();
        passed.put("red", false);
        passed.put("blue", false);

        // Add time if under 7s
        if (secondsRemaining < 7) {
            secondsRemaining = 7;
        }

        ChatColor color = (team != null) ? team.getColor() : ChatColor.GOLD;
        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + color + captain.getName() + " (" + TextUtil.toTiny(teamId) + ") " +
                ChatColor.YELLOW + TextUtil.toTiny("puja ") + ChatColor.GREEN + currentBid + " créditos" +
                ChatColor.GRAY + " (⏱ " + secondsRemaining + "s)");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        }

        refreshGUI();
        return true;
    }

    public void pass(Player captain) {
        if (!active) return;
        TTRCore plugin = TTRCore.getInstance();
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(captain);
        if (team == null) return;

        String teamId = team.getIdentifier().toLowerCase();
        passed.put(teamId, true);
        captain.sendMessage(TTRPrefix.TTR_GAME + TextUtil.toTiny("Has pasado tu turno en esta subasta."));

        if (hasPassed("red") && hasPassed("blue")) {
            secondsRemaining = 0;
        }
        refreshGUI();
    }

    public void refreshGUI() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTitle().contains(TextUtil.toTiny("Subasta de Miembros"))) {
                AuctionDraftGUI.update(p, this);
            } else if (active && (p.isOp() || isCaptain(p))) {
                AuctionDraftGUI.open(p, this);
            }
        }
    }

    private boolean isCaptain(Player p) {
        TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(p);
        return team != null && team.isLeader(p.getUniqueId());
    }

    public void concludeDraft() {
        cancelTaskOnly();
        this.active = false;
        currentCandidate = null;

        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "✔ " +
                TextUtil.toTiny("¡LA SUBASTA HA CONCLUIDO! TODOS LOS EQUIPOS ESTÁN COMPLETOS.") + " ✔");
        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            if (p.getOpenInventory().getTitle().contains(TextUtil.toTiny("Subasta de Miembros"))) {
                p.closeInventory();
            }
        }
    }

    public void cancelDraft() {
        cancelTaskOnly();
        this.active = false;
        this.currentCandidate = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTitle().contains(TextUtil.toTiny("Subasta de Miembros"))) {
                p.closeInventory();
            }
        }
    }

    private void cancelTaskOnly() {
        if (draftTask != null) {
            draftTask.cancel();
            draftTask = null;
        }
    }
}
