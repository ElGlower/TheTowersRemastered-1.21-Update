package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceJoinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /ttrforcejoin <Jugador> <Equipo>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        String teamNameArg = args[1];
        String realTeamName = null;

        if (TTRCore.getInstance().getConfigManager().getTeamNames() != null) {
            for (String t : TTRCore.getInstance().getConfigManager().getTeamNames()) {
                if (t.equalsIgnoreCase(teamNameArg)) {
                    realTeamName = t;
                    break;
                }
            }
        }

        if (realTeamName == null) {
            sender.sendMessage(ChatColor.RED + "El equipo '" + teamNameArg + "' no existe.");
            return true;
        }
        TTRCore.getInstance().getTeamHandler().addPlayerToTeam(target, realTeamName);
        if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            TTRCore.getInstance().getCurrentMatch().joinPlayerToMatch(target);
        }

        sender.sendMessage(ChatColor.GREEN + "Jugador " + target.getName() + " forzado a entrar en " + realTeamName);

        return true;
    }
}