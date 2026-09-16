package me.PauMAVA.TTR.commands;
import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.match.MatchStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReviveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) return true;
        if (args.length == 0) { sender.sendMessage("Uso: /ttrrevive <jugador>"); return true; }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { sender.sendMessage("Jugador no encontrado."); return true; }

        if (TTRCore.getInstance().getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            sender.sendMessage("No hay partida en curso.");
            return true;
        }

        TTRTeam team = TTRCore.getInstance().getTeamHandler().getPlayerTeam(target);
        if (team != null) {
            Location spawn = TTRCore.getInstance().getConfigManager().getTeamSpawn(team.getIdentifier());
            if (spawn != null) {
                target.teleport(spawn);
                target.setGameMode(org.bukkit.GameMode.SURVIVAL);
                sender.sendMessage(ChatColor.GREEN + "Has revivido a " + target.getName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "El jugador no tiene equipo.");
        }
        return true;
    }
}