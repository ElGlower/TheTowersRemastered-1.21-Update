package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.DestinyTheme;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import me.PauMAVA.TTR.web.WebStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player targetPlayer = null;

        if (args.length > 0) {
            targetPlayer = Bukkit.getPlayer(args[0]);
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        }

        String targetName;
        int kills = 0;
        int deaths = 0;
        int goals = 0;
        int wins = 0;
        String teamOrRole;

        if (targetPlayer != null) {
            targetName = targetPlayer.getName();
            Map<String, Object> stats = WebStatsManager.getInstance().getPlayerStats(targetPlayer.getUniqueId());
            if (stats != null) {
                kills = (int) stats.getOrDefault("kills", 0);
                deaths = (int) stats.getOrDefault("deaths", 0);
                goals = (int) stats.getOrDefault("goals", 0);
                wins = (int) stats.getOrDefault("wins", 0);
            }

            if (TTRCore.getInstance().getCurrentMatch() != null) {
                int matchKills = TTRCore.getInstance().getCurrentMatch().getKills(targetPlayer);
                if (matchKills > kills) kills = matchKills;
            }

            TTRTeam team = TTRCore.getInstance().getTeamHandler() != null ? TTRCore.getInstance().getTeamHandler().getPlayerTeam(targetPlayer) : null;
            if (team != null) {
                teamOrRole = team.getColor() + TextUtil.toTiny(team.getIdentifier());
            } else if (TTRCore.isAdmin(targetPlayer)) {
                teamOrRole = DestinyTheme.DESTINY_ROLE_BADGE;
            } else {
                teamOrRole = ChatColor.GRAY + TextUtil.toTiny("Lobby");
            }
        } else if (args.length > 0) {
            targetName = args[0];
            Map<String, Object> stats = WebStatsManager.getInstance().getPlayerStatsByName(targetName);
            if (stats != null) {
                kills = (int) stats.getOrDefault("kills", 0);
                deaths = (int) stats.getOrDefault("deaths", 0);
                goals = (int) stats.getOrDefault("goals", 0);
                wins = (int) stats.getOrDefault("wins", 0);
            }
            teamOrRole = ChatColor.GRAY + TextUtil.toTiny("Desconectado");
        } else {
            sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /stats <jugador>"));
            return true;
        }

        double kd = (deaths == 0) ? kills : Math.round(((double) kills / deaths) * 100.0) / 100.0;

        sender.sendMessage(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        sender.sendMessage("      " + ChatColor.GOLD + "★ " + ChatColor.YELLOW + "§l" + TextUtil.toTiny("ESTADÍSTICAS DEL JUGADOR") + ChatColor.GOLD + " ★");
        sender.sendMessage("   " + ChatColor.GRAY + TextUtil.toTiny("Jugador: ") + ChatColor.WHITE + targetName + " §8[" + teamOrRole + "§8]");
        sender.sendMessage("");
        sender.sendMessage("   " + ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Asesinatos: ") + ChatColor.GREEN + TextUtil.toTiny(String.valueOf(kills)));
        sender.sendMessage("   " + ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Muertes:    ") + ChatColor.RED + TextUtil.toTiny(String.valueOf(deaths)));
        sender.sendMessage("   " + ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("K/D Ratio:  ") + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(kd)));
        sender.sendMessage("   " + ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Goles:      ") + ChatColor.AQUA + TextUtil.toTiny(String.valueOf(goals)));
        sender.sendMessage("   " + ChatColor.GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny("Victorias:  ") + ChatColor.LIGHT_PURPLE + TextUtil.toTiny(String.valueOf(wins)));
        sender.sendMessage(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        return true;
    }
}
