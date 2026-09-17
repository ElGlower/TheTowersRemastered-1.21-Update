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
                    plugin.setCurrentSelectionMode(plugin.getCurrentSelectionMode().next());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "stop_phase": {
                    plugin.getLeaderVoteManager().cancelVoting();
                    plugin.getAuctionDraftManager().cancelDraft();
                    player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Fase activa detenida con éxito."));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "start_voting": {
                    plugin.getLeaderVoteManager().startVoting(true);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "start_draft": {
                    plugin.getAuctionDraftManager().startDraft();
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
                            player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugador expulsado del equipo."));
                        } else if (click.isRightClick()) {
                            // Move to opposite team
                            String other = currentTeam.getIdentifier().equalsIgnoreCase("red") ? "Blue" : "Red";
                            TTRTeam targetTeam = plugin.getTeamHandler().getTeam(other);
                            if (targetTeam != null) {
                                currentTeam.getPlayers().remove(targetUuid);
                                if (currentTeam.isLeader(targetUuid)) currentTeam.clearLeader();

                                targetTeam.getPlayers().add(targetUuid);
                                if (targetPlayer != null && targetTeam.getSpawnPoint() != null) {
                                    targetPlayer.teleport(targetTeam.getSpawnPoint());
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
            }
        }
    }
}
