package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin") && !sender.hasPermission("destinytowers.admin") && !sender.isOp()) {
            sender.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permisos.");
            return true;
        }

        TTRCore plugin = TTRCore.getInstance();

        if (plugin.getModeAnnouncementManager() != null) {
            plugin.getModeAnnouncementManager().cancel();
        }
        if (plugin.getLeaderVoteManager() != null) {
            plugin.getLeaderVoteManager().cancelVoting();
        }
        if (plugin.getAuctionDraftManager() != null) {
            plugin.getAuctionDraftManager().cancelDraft();
        }
        if (plugin.getAutoStarter() != null) {
            plugin.getAutoStarter().cancelCountdown();
        }

        if (plugin.getCurrentMatch() != null) {
            if (plugin.getCurrentMatch().isOnCourse()) {
                plugin.getCurrentMatch().endMatch(null);
            }
        }

        plugin.resetMatchLogic();

        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.RED + "" + ChatColor.BOLD +
                me.PauMAVA.TTR.util.TextUtil.toTiny("¡Partida / Fase cancelada y restablecida por la administración!"));
        Bukkit.broadcastMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        sender.sendMessage(TTRPrefix.TTR_SUCCESS + me.PauMAVA.TTR.util.TextUtil.toTiny("Partida y estados reseteados completamente al lobby."));
        return true;
    }
}