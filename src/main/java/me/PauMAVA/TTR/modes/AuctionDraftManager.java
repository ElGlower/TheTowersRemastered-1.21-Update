package me.PauMAVA.TTR.modes;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.AuctionDraftGUI;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    public int getStartingCredits() { return TTRCore.getInstance().getConfig().getInt("auction.initial_credits", 100); }
    public int getTurnDuration() { return TTRCore.getInstance().getConfig().getInt("auction.turn_seconds", 15); }
    public int getTeamCredits(String team) { return credits.getOrDefault(team.toLowerCase(), getStartingCredits()); }
    public boolean hasPassed(String team) { return passed.getOrDefault(team.toLowerCase(), false); }

    public void startDraft() {
        TTRCore plugin = TTRCore.getInstance();
        cancelDraft();

        int startingCredits = getStartingCredits();
        credits.put("red", startingCredits);
        credits.put("blue", startingCredits);
        passed.put("red", false);
        passed.put("blue", false);

        TTRTeam red = plugin.getTeamHandler().getTeam("Red");
        TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");

        if ((red != null && red.getLeader() == null) || (blue != null && blue.getLeader() == null)) {
            // Intentar asignar automáticamente el primer miembro como líder si ya existen miembros (no administradores)
            if (red != null && red.getLeader() == null && !red.getPlayers().isEmpty()) {
                for (UUID u : red.getPlayers()) {
                    if (!TTRCore.isAdmin(u)) { red.setLeader(u); break; }
                }
            }
            if (blue != null && blue.getLeader() == null && !blue.getPlayers().isEmpty()) {
                for (UUID u : blue.getPlayers()) {
                    if (!TTRCore.isAdmin(u)) { blue.setLeader(u); break; }
                }
            }

            if ((red != null && red.getLeader() == null) || (blue != null && blue.getLeader() == null)) {
                Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.GOLD + "" + ChatColor.BOLD +
                        TextUtil.toTiny("Antes de la subasta, cada equipo debe elegir a su líder por votación."));
                plugin.setCurrentSelectionMode(TeamSelectionMode.AUCTION_DRAFT);
                plugin.getLeaderVoteManager().startVoting(false);
                return;
            }
        }

        // Populate pool with players who are not leaders and NOT admins
        pool.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TTRCore.isAdmin(p)) continue; // Administradores no se subastan
            TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);
            if (team != null && team.isLeader(p.getUniqueId())) {
                continue; // Leaders don't get auctioned
            }
            pool.add(p.getUniqueId());
        }

        if (pool.isEmpty()) {
            // Todos los jugadores conectados ya son capitanes (caso 1v1 con 2 jugadores)
            Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "★ " +
                    TextUtil.toTiny("¡Equipos conformados con sus líderes! Todo listo para el combate.") + " ★");
            Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            int prestart = plugin.getConfig().getInt("match.prestart_countdown", 10);
            plugin.getAutoStarter().startMatchCountdown(prestart);
            return;
        }

        Collections.shuffle(pool);
        this.active = true;

        // Entregar ítem interactivo del panel de subasta a los líderes
        giveLeaderAuctionItem(red);
        giveLeaderAuctionItem(blue);

        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "⚖ " +
                TextUtil.toTiny("¡COMIENZA LA SUBASTA DE JUGADORES (AUCTION DRAFT)!") + " ⚖");
        Bukkit.broadcastMessage(ChatColor.GRAY + TextUtil.toTiny("Cada capitán tiene " + startingCredits + " créditos para pujar por sus compañeros."));
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

                // Actionbar informativa en tiempo real
                Player candPlayer = (currentCandidate != null) ? Bukkit.getPlayer(currentCandidate) : null;
                String cName = (candPlayer != null) ? candPlayer.getName() : "Jugador";
                String ab = TextUtil.color("&#FFFFFF⚖ " + TextUtil.toTiny("Subasta: ") + "&#FFFF55" + cName +
                        " &#888888| " + "&#FFFFFF⏱ " + "&#FF2E2E§l" + secondsRemaining + "s" +
                        " &#888888| " + TextUtil.toTiny("Oferta: ") + "&#55FF55" + currentBid + "c");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(net.kyori.adventure.text.Component.text(ab));
                }

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
                if (TTRCore.isAdmin(p)) continue;
                red.setLeader(p.getUniqueId());
                red.addPlayer(p);
                break;
            }
        }
        if (blue != null && blue.getLeader() == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (TTRCore.isAdmin(p)) continue;
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
        secondsRemaining = getTurnDuration();
        passed.put("red", false);
        passed.put("blue", false);

        Player candidatePlayer = Bukkit.getPlayer(currentCandidate);
        String name = (candidatePlayer != null) ? candidatePlayer.getName() : "Jugador";

        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "" + ChatColor.BOLD +
                TextUtil.toTiny("En subasta: ") + ChatColor.WHITE + name +
                ChatColor.GRAY + TextUtil.toTiny(" | Puja inicial: 0 créditos. Tienes ") + secondsRemaining + "s.");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            AuctionDraftGUI.open(p, this);
        }
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

        if (getTeamCredits("red") <= 0 && getTeamCredits("blue") <= 0) {
            autoBalanceRemainingPool();
            concludeDraft();
            return;
        }

        nextCandidate();
    }

    public void autoBalanceRemainingPool() {
        TTRCore plugin = TTRCore.getInstance();
        TTRTeam red = plugin.getTeamHandler().getTeam("Red");
        TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");

        List<UUID> toAssign = new ArrayList<>();
        if (currentCandidate != null) {
            toAssign.add(currentCandidate);
            currentCandidate = null;
        }
        toAssign.addAll(pool);
        pool.clear();

        for (UUID u : toAssign) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                int rCount = (red != null) ? red.getPlayers().size() : 0;
                int bCount = (blue != null) ? blue.getPlayers().size() : 0;
                String assigned = (rCount <= bCount) ? "Red" : "Blue";
                plugin.getTeamHandler().addPlayerToTeam(p, assigned);
            }
        }

        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "" + ChatColor.BOLD +
                TextUtil.toTiny("¡Ambos capitanes se quedaron sin créditos! Los jugadores restantes se han asignado equitativamente."));
    }

    public void skipCandidate(Player requester) {
        if (!active || currentCandidate == null) return;
        if (requester != null && !requester.isOp() && !isCaptain(requester)) {
            requester.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Solo los capitanes o administradores pueden saltar jugadores."));
            return;
        }

        Player candidate = Bukkit.getPlayer(currentCandidate);
        String name = (candidate != null) ? candidate.getName() : "Jugador";

        TTRCore plugin = TTRCore.getInstance();
        TTRTeam red = plugin.getTeamHandler().getTeam("Red");
        TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");
        int rCount = (red != null) ? red.getPlayers().size() : 0;
        int bCount = (blue != null) ? blue.getPlayers().size() : 0;
        String assigned = (rCount <= bCount) ? "Red" : "Blue";

        if (candidate != null) {
            plugin.getTeamHandler().addPlayerToTeam(candidate, assigned);
        }

        TTRTeam wonTeam = plugin.getTeamHandler().getTeam(assigned);
        ChatColor col = (wonTeam != null) ? wonTeam.getColor() : ChatColor.WHITE;

        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + TextUtil.toTiny("Subasta omitida para ") +
                ChatColor.WHITE + name + ChatColor.YELLOW + TextUtil.toTiny(". Asignado al equipo ") + col + TextUtil.toTiny(assigned));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
        }

        nextCandidate();
    }

    public void addCredits(String team, int amount) {
        String key = team.toLowerCase();
        int cur = getTeamCredits(key);
        credits.put(key, Math.max(0, cur + amount));
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Créditos del equipo ") +
                ChatColor.WHITE + team.toUpperCase() + ChatColor.YELLOW + TextUtil.toTiny(" actualizados: ") +
                ChatColor.GREEN + (cur + amount) + " créditos" + ChatColor.GRAY + " (" + (amount >= 0 ? "+" : "") + amount + ")");
        refreshGUI();
    }

    public void setCredits(String team, int amount) {
        String key = team.toLowerCase();
        credits.put(key, Math.max(0, amount));
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Créditos del equipo ") +
                ChatColor.WHITE + team.toUpperCase() + ChatColor.YELLOW + TextUtil.toTiny(" fijados en: ") +
                ChatColor.GREEN + amount + " créditos");
        refreshGUI();
    }

    private void giveLeaderAuctionItem(TTRTeam team) {
        if (team == null || team.getLeader() == null) return;
        Player leader = Bukkit.getPlayer(team.getLeader());
        if (leader != null) {
            org.bukkit.inventory.ItemStack panelItem = new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT);
            org.bukkit.inventory.meta.ItemMeta meta = panelItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "⚖ " + TextUtil.toTiny("Panel de Subasta") + ChatColor.GRAY + " (" + TextUtil.toTiny("Clic Derecho") + ")");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + TextUtil.toTiny("Haz clic derecho para abrir la mesa de pujas en cualquier momento."));
                meta.setLore(lore);
                panelItem.setItemMeta(meta);
            }
            leader.getInventory().setItem(0, panelItem);
        }
    }

    public boolean bid(Player captain, int amountToAdd) {
        if (!active || currentCandidate == null) return false;
        TTRCore plugin = TTRCore.getInstance();
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(captain);

        if (team == null || !team.isLeader(captain.getUniqueId())) {
            captain.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Solo los capitanes de equipo pueden pujar."));
            return false;
        }

        String teamId = team.getIdentifier().toLowerCase();
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

        ChatColor color = team.getColor();
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
        if (team == null || !team.isLeader(captain.getUniqueId())) {
            captain.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Solo los capitanes de equipo pueden pasar turno."));
            return;
        }

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
            if (p.getInventory().contains(Material.GOLD_INGOT)) {
                p.getInventory().remove(Material.GOLD_INGOT);
            }
        }

        int prestart = TTRCore.getInstance().getConfig().getInt("match.prestart_countdown", 15);
        Bukkit.getScheduler().runTaskLater(TTRCore.getInstance(), () -> {
            TTRCore.getInstance().getCurrentMatch().startPreparationPhase(prestart);
        }, 40L);
    }

    public void cancelDraft() {
        cancelTaskOnly();
        this.active = false;
        this.currentCandidate = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTitle().contains(TextUtil.toTiny("Subasta de Miembros"))) {
                p.closeInventory();
            }
            if (p.getInventory().contains(Material.GOLD_INGOT)) {
                p.getInventory().remove(Material.GOLD_INGOT);
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
