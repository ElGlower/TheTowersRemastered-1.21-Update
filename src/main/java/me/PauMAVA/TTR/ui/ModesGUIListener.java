package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.modes.LeaderVoteManager;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ModesGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        TTRCore plugin = TTRCore.getInstance();

        // 1. Leader Vote GUI
        if (title.contains(TextUtil.toTiny("Votación de Líder"))) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;

            String candStr = item.getItemMeta().getPersistentDataContainer().get(LeaderVoteGUI.KEY_CANDIDATE, PersistentDataType.STRING);
            if (candStr != null) {
                try {
                    UUID candUuid = UUID.fromString(candStr);
                    if (plugin.getLeaderVoteManager().castVote(player, candUuid)) {
                        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);
                        if (team != null) {
                            LeaderVoteManager.TeamVoteState state = plugin.getLeaderVoteManager().getState(team.getIdentifier());
                            if (state != null) {
                                LeaderVoteGUI.open(player, state);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
            return;
        }

        // 2. Auction Draft GUI
        if (title.equals(AuctionDraftGUI.TITLE)) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;

            String action = item.getItemMeta().getPersistentDataContainer().get(AuctionDraftGUI.KEY_ACTION, PersistentDataType.STRING);
            if (action == null) return;

            switch (action) {
                case "bid_10" -> plugin.getAuctionDraftManager().bid(player, 10);
                case "bid_25" -> plugin.getAuctionDraftManager().bid(player, 25);
                case "bid_50" -> plugin.getAuctionDraftManager().bid(player, 50);
                case "pass" -> plugin.getAuctionDraftManager().pass(player);
            }
            return;
        }

        // 3. Admin Leaders GUI
        if (title.equals(AdminLeadersGUI.TITLE)) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;

            String action = item.getItemMeta().getPersistentDataContainer().get(AdminLeadersGUI.KEY_ACTION, PersistentDataType.STRING);
            if (action == null) return;

            ClickType click = event.getClick();

            switch (action) {
                case "cycle_mode": {
                    me.PauMAVA.TTR.modes.TeamSelectionMode nextMode = plugin.getCurrentSelectionMode().next();
                    plugin.setCurrentSelectionMode(nextMode);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    plugin.getModeAnnouncementManager().announceMode(nextMode, null);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "open_settings": {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    ModeSettingsGUI.open(player);
                    break;
                }
                case "skip_announcement": {
                    plugin.getModeAnnouncementManager().forceStartNow();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "stop_phase": {
                    plugin.getModeAnnouncementManager().cancel();
                    plugin.getLeaderVoteManager().cancelVoting();
                    plugin.getAuctionDraftManager().cancelDraft();
                    if (plugin.getAutoStarter() != null) {
                        plugin.getAutoStarter().cancelCountdown();
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.setLevel(0);
                        p.setExp(0.0f);
                        if (p.getOpenInventory() != null && (p.getOpenInventory().getTitle().contains(TextUtil.toTiny("Votación de Líder")) || p.getOpenInventory().getTitle().equals(AuctionDraftGUI.TITLE))) {
                            p.closeInventory();
                        }
                    }
                    Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + TextUtil.color("&#FF2E2E" + TextUtil.toTiny("¡La fase activa ha sido cancelada por un administrador!")));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "start_draft": {
                    plugin.getModeAnnouncementManager().announceMode(me.PauMAVA.TTR.modes.TeamSelectionMode.AUCTION_DRAFT, () -> {
                        plugin.getAuctionDraftManager().startDraft();
                    });
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "start_standard": {
                    plugin.getModeAnnouncementManager().announceMode(me.PauMAVA.TTR.modes.TeamSelectionMode.STANDARD, () -> {
                        plugin.getAutoStarter().checkStart();
                    });
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "back": {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    ConfigGUI.open(player);
                    break;
                }
                case "player_manage": {
                    String uuidStr = item.getItemMeta().getPersistentDataContainer().get(AdminLeadersGUI.KEY_PLAYER_UUID, PersistentDataType.STRING);
                    if (uuidStr == null) return;

                    try {
                        UUID targetUuid = UUID.fromString(uuidStr);
                        Player targetPlayer = Bukkit.getPlayer(targetUuid);
                        TTRTeam currentTeam = (targetPlayer != null) ? plugin.getTeamHandler().getPlayerTeam(targetPlayer) : null;
                        if (currentTeam == null) {
                            // Find team from offline records
                            for (TTRTeam t : plugin.getTeamHandler().getTeams()) {
                                if (t.getPlayers().contains(targetUuid)) {
                                    currentTeam = t;
                                    break;
                                }
                            }
                        }
                        if (currentTeam == null) return;

                        if (click.isShiftClick()) {
                            // Kick from team
                            currentTeam.getPlayers().remove(targetUuid);
                            if (currentTeam.isLeader(targetUuid)) currentTeam.clearLeader();
                            if (targetPlayer != null) {
                                me.PauMAVA.TTR.voice.VoiceChatManager.getInstance().assignPlayerToTeamVoice(targetPlayer, null);
                            }
                            player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugador expulsado del equipo."));
                        } else if (click.isRightClick()) {
                            // Move to opposite team
                            String other = currentTeam.getIdentifier().equalsIgnoreCase("red") ? "Blue" : "Red";
                            TTRTeam targetTeam = plugin.getTeamHandler().getTeam(other);
                            if (targetTeam != null) {
                                currentTeam.getPlayers().remove(targetUuid);
                                if (currentTeam.isLeader(targetUuid)) currentTeam.clearLeader();

                                targetTeam.getPlayers().add(targetUuid);
                                if (targetPlayer != null) {
                                    if (targetTeam.getSpawnPoint() != null) {
                                        targetPlayer.teleport(targetTeam.getSpawnPoint());
                                    }
                                    me.PauMAVA.TTR.voice.VoiceChatManager.getInstance().assignPlayerToTeamVoice(targetPlayer, other);
                                }
                                player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugador transferido a ") + targetTeam.getColor() + targetTeam.getIdentifier());
                            }
                        } else {
                            // Toggle Leader
                            if (currentTeam.isLeader(targetUuid)) {
                                currentTeam.clearLeader();
                                player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Rango de líder removido."));
                            } else {
                                plugin.getLeaderVoteManager().crownLeader(currentTeam, targetUuid);
                            }
                        }

                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                        AdminLeadersGUI.open(player);
                    } catch (Exception ignored) {}
                    break;
                }
                case "player_assign_unassigned": {
                    String uuidStr = item.getItemMeta().getPersistentDataContainer().get(AdminLeadersGUI.KEY_PLAYER_UUID, PersistentDataType.STRING);
                    if (uuidStr == null) return;

                    try {
                        UUID targetUuid = UUID.fromString(uuidStr);
                        Player targetPlayer = Bukkit.getPlayer(targetUuid);
                        if (targetPlayer == null || !targetPlayer.isOnline()) {
                            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El jugador ya no está conectado."));
                            AdminLeadersGUI.open(player);
                            return;
                        }

                        String targetTeamName = click.isRightClick() ? "Blue" : "Red";
                        TTRTeam targetTeam = plugin.getTeamHandler().getTeam(targetTeamName);
                        if (targetTeam != null) {
                            plugin.getTeamHandler().addPlayerToTeam(targetPlayer, targetTeamName);
                            player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugador asignado a ") + targetTeam.getColor() + targetTeam.getIdentifier());
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                        }
                        AdminLeadersGUI.open(player);
                    } catch (Exception ignored) {}
                    break;
                }
            }
            return;
        }

        // 4. Mode Settings GUI
        if (title.equals(ModeSettingsGUI.TITLE)) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;

            String action = item.getItemMeta().getPersistentDataContainer().get(ModeSettingsGUI.KEY_ACTION, PersistentDataType.STRING);
            if (action == null) return;

            switch (action) {
                case "cycle_vote_secs": {
                    int[] opts = {10, 15, 20, 30, 45, 60};
                    int cur = plugin.getConfig().getInt("voting.round_seconds", 20);
                    int next = getNextOption(opts, cur);
                    plugin.getConfig().set("voting.round_seconds", next);
                    plugin.saveConfig();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                    ModeSettingsGUI.open(player);
                    break;
                }
                case "cycle_turn_secs": {
                    int[] opts = {10, 15, 20, 25, 30};
                    int cur = plugin.getConfig().getInt("auction.turn_seconds", 15);
                    int next = getNextOption(opts, cur);
                    plugin.getConfig().set("auction.turn_seconds", next);
                    plugin.saveConfig();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                    ModeSettingsGUI.open(player);
                    break;
                }
                case "cycle_credits": {
                    int[] opts = {50, 100, 150, 200, 250, 300};
                    int cur = plugin.getConfig().getInt("auction.initial_credits", 100);
                    int next = getNextOption(opts, cur);
                    plugin.getConfig().set("auction.initial_credits", next);
                    plugin.saveConfig();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                    ModeSettingsGUI.open(player);
                    break;
                }
                case "cycle_read_secs": {
                    int[] opts = {0, 15, 30, 45, 60, 90};
                    int cur = plugin.getConfig().getInt("modes.explanation_seconds", 60);
                    int next = getNextOption(opts, cur);
                    plugin.getConfig().set("modes.explanation_seconds", next);
                    plugin.saveConfig();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.4f);
                    ModeSettingsGUI.open(player);
                    break;
                }
                case "back_leaders": {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    AdminLeadersGUI.open(player);
                    break;
                }
            }
        }
    }

    private int getNextOption(int[] options, int current) {
        for (int i = 0; i < options.length; i++) {
            if (options[i] == current) {
                return options[(i + 1) % options.length];
            }
        }
        return options[0];
    }
}
